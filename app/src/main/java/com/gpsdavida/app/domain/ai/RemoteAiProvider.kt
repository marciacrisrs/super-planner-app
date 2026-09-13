package com.superplanner.app.domain.ai

import com.superplanner.app.BuildConfig
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.planning.DayReorganizationOperation
import com.superplanner.app.domain.planning.DayReorganizationRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** Remote provider. The LLM is accessed only through the Super Planner AI Gateway. */
class RemoteAiProvider @Inject constructor() : AiProvider {
    override suspend fun interpret(request: AiRequest): AiProposal = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.AI_GATEWAY_URL.trim().trimEnd('/')
        require(endpoint.isNotEmpty()) { "AI gateway is not configured" }

        val connection = (URL("$endpoint/v1/ai/propose").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val context = JSONObject().apply {
                request.context.nowIso?.let { put("nowIso", it) }
                request.context.activeActivityId?.let { put("activeActivityId", it) }
                put("minimalRouteFacts", JSONArray(request.context.minimalRouteFacts))
            }
            val body = JSONObject().apply {
                put("message", request.message)
                put("context", context)
            }

            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (status !in 200..299) error("AI gateway returned HTTP $status")

            mapProposal(JSONObject(responseBody).getJSONObject("proposal"), request)
        } finally {
            connection.disconnect()
        }
    }

    private fun mapProposal(json: JSONObject, request: AiRequest): AiProposal {
        val commandType = json.getString("commandType")
        val explanation = json.getString("explanation")
        val requiresConfirmation = json.getBoolean("requiresConfirmation")
        val payload = json.getJSONObject("payload")

        val command = when (commandType) {
            "CREATE_ACTIVITY_DRAFT" -> AiCommand.CreateActivityDraft(mapActivityDraft(payload, request))
            "EXPLAIN_NEXT_ACTIVITY" -> AiCommand.ExplainNextActivity(
                activityId = payload.optString("activityId").ifBlank { request.context.activeActivityId.orEmpty() },
                evidence = payload.getJSONArray("evidence").toStringList(),
            )
            "REORGANIZE_DAY" -> {
                val activityId = request.context.activeActivityId.orEmpty()
                val now = request.context.nowIso?.let(Instant::parse)
                val minutes = payload.optInt("delayMinutes", 0).toLong()
                if (activityId.isBlank() || now == null || minutes <= 0) {
                    AiCommand.MissingInformation(
                        buildList {
                            if (activityId.isBlank()) add("atividade ativa")
                            if (now == null) add("horário atual")
                            if (minutes <= 0) add("quantidade de minutos")
                        },
                    )
                } else {
                    AiCommand.ReorganizeDay(
                        DayReorganizationRequest(
                            operation = DayReorganizationOperation.DelayActivity(
                                activityId = ActivityInstanceId(activityId),
                                minutes = minutes,
                            ),
                            now = now,
                        ),
                    )
                }
            }
            "MISSING_INFORMATION" -> AiCommand.MissingInformation(payload.getJSONArray("fields").toStringList())
            "RECALCULATE_ROUTE" -> AiCommand.RecalculateRoute
            else -> error("Unsupported AI command: $commandType")
        }

        return AiProposal(command, explanation, requiresConfirmation)
    }

    private fun mapActivityDraft(payload: JSONObject, request: AiRequest): NaturalLanguageActivityDraft {
        val today = request.context.nowIso?.take(10)?.let(LocalDate::parse) ?: LocalDate.now()
        val title = payload.optString("title").trim()
        val durationMinutes = payload.optInt("durationMinutes", 0)
        val date = payload.optString("date").takeIf(String::isNotBlank)?.let(LocalDate::parse)
        val startTime = payload.optString("startTime").takeIf(String::isNotBlank)?.let(LocalTime::parse)
        val priority = payload.optString("priority")
            .takeIf(String::isNotBlank)
            ?.let { runCatching { Priority.valueOf(it) }.getOrNull() }
            ?: Priority.IMPORTANT
        val energy = payload.optString("energy")
            .takeIf(String::isNotBlank)
            ?.let { runCatching { Energy.valueOf(it) }.getOrNull() }

        val parserDraft = NaturalLanguageActivityParser.parse(request.message, today)
        val missing = buildSet {
            if (title.isBlank()) add(MissingActivityField.TITLE)
            if (durationMinutes <= 0) add(MissingActivityField.DURATION)
        }

        return NaturalLanguageActivityDraft(
            sourceText = request.message,
            title = title,
            plannedDuration = durationMinutes.takeIf { it > 0 }?.let { Duration.ofMinutes(it.toLong()) },
            date = date,
            startTime = startTime,
            recurrence = parserDraft.recurrence,
            priority = priority,
            energy = energy,
            missingFields = missing,
        )
    }
}

private fun JSONArray.toStringList(): List<String> = buildList {
    for (index in 0 until length()) add(getString(index))
}

/** Uses the remote gateway when configured and keeps the app usable without network/AI. */
class HybridAiProvider @Inject constructor(
    private val remote: RemoteAiProvider,
    private val local: RuleBasedAiProvider,
) : AiProvider {
    override suspend fun interpret(request: AiRequest): AiProposal {
        if (BuildConfig.AI_GATEWAY_URL.isBlank()) return local.interpret(request)
        return runCatching { remote.interpret(request) }.getOrElse { local.interpret(request) }
    }
}
