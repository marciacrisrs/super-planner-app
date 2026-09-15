package com.superplanner.app.domain.ai

import com.superplanner.app.BuildConfig
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.planning.DayReorganizationOperation
import com.superplanner.app.domain.planning.DayReorganizationRequest
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class RemoteAiProvider @Inject constructor(private val telemetry: AiTelemetry) : AiProvider {
    override suspend fun interpret(request: AiRequest): AiProposal = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.AI_GATEWAY_URL.trim().trimEnd('/')
        require(endpoint.isNotEmpty()) { "AI gateway is not configured" }
        val requestId = UUID.randomUUID().toString()
        val response = postAiGateway(
            path = "/v1/ai/propose", endpoint = endpoint, requestId = requestId,
            timeoutMs = 20_000, schemaVersion = AI_PROPOSAL_SCHEMA_VERSION,
            body = AiGatewayProtocol.buildRequestBody(request).toString(),
        )
        if (response.status !in 200..299) error("AI gateway returned HTTP ${response.status}: ${AiGatewayProtocol.parseError(response.body, requestId)}")
        require(response.body.isNotBlank()) { "AI gateway returned an empty response [requestId=$requestId]" }
        val root = JSONObject(response.body)
        val proposal = root.optJSONObject("proposal") ?: error("AI gateway response is missing proposal [requestId=$requestId]")
        AiGatewayProposalValidator.validate(proposal)
        val mapped = mapProposal(proposal, request)
        telemetry.remoteSucceeded(commandTypeOf(mapped.command))
        mapped
    }

    private fun mapProposal(json: JSONObject, request: AiRequest): AiProposal {
        val commandType = json.getString("commandType")
        val explanation = json.getString("explanation")
        val requiresConfirmation = json.getBoolean("requiresConfirmation")
        val payload = json.getJSONObject("payload")
        val command = when (commandType) {
            "CREATE_ACTIVITY_DRAFT" -> AiCommand.CreateActivityDraft(AiGatewayProposalMapper.mapCreateActivityDraft(payload, request))
            "EXPLAIN_NEXT_ACTIVITY" -> AiCommand.ExplainNextActivity(payload.optString("activityId").ifBlank { request.context.activeActivityId.orEmpty() }, payload.getJSONArray("evidence").toStringList())
            "REORGANIZE_DAY" -> {
                val activityId = request.context.activeActivityId.orEmpty()
                val now = request.context.nowIso?.let(Instant::parse)
                val minutes = payload.optInt("delayMinutes", 0).toLong()
                if (activityId.isBlank() || now == null || minutes <= 0) AiCommand.MissingInformation(buildList {
                    if (activityId.isBlank()) add("atividade ativa")
                    if (now == null) add("horário atual")
                    if (minutes <= 0) add("quantidade de minutos")
                }) else AiCommand.ReorganizeDay(DayReorganizationRequest(DayReorganizationOperation.DelayActivity(ActivityInstanceId(activityId), minutes), now))
            }
            "MISSING_INFORMATION" -> AiCommand.MissingInformation(payload.getJSONArray("fields").toStringList())
            "RECALCULATE_ROUTE" -> AiCommand.RecalculateRoute
            else -> error("Unsupported AI command: $commandType")
        }
        return AiProposal(command, explanation, requiresConfirmation)
    }

    private fun commandTypeOf(command: AiCommand): String = when (command) {
        is AiCommand.CreateActivityDraft -> "CREATE_ACTIVITY_DRAFT"
        is AiCommand.ExplainNextActivity -> "EXPLAIN_NEXT_ACTIVITY"
        is AiCommand.ReorganizeDay -> "REORGANIZE_DAY"
        is AiCommand.MissingInformation -> "MISSING_INFORMATION"
        AiCommand.RecalculateRoute -> "RECALCULATE_ROUTE"
    }
}

private fun JSONArray.toStringList(): List<String> = buildList { for (index in 0 until length()) add(getString(index)) }

class HybridAiProvider @Inject constructor(private val remote: RemoteAiProvider, private val local: RuleBasedAiProvider, private val telemetry: AiTelemetry) : AiProvider {
    override suspend fun interpret(request: AiRequest): AiProposal {
        if (BuildConfig.AI_GATEWAY_URL.isBlank()) { telemetry.localFallback("gateway_not_configured"); return local.interpret(request) }
        return runCatching { remote.interpret(request) }.getOrElse { error ->
            if (AiFallbackPolicy.shouldFallback(error)) { telemetry.localFallback(error::class.simpleName ?: "transport_error"); local.interpret(request) }
            else { telemetry.remoteRejected(error::class.simpleName ?: "contract_error"); throw error }
        }
    }
}
