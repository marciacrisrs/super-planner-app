package com.gpsdavida.app.domain.ai

import com.superplanner.app.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class RemoteNextActionRecommendation(
    val candidateId: String?,
    val alternatives: List<String>,
    val reason: String,
    val confidence: String,
    val requestId: String,
    val model: String,
)

/** Calls the Gateway only with domain-eligible candidate ids; feasibility stays app-owned. */
class NextActionGatewayClient @Inject constructor() {
    suspend fun recommend(request: AiRequest, eligibleCandidateIds: List<String>): RemoteNextActionRecommendation =
        withContext(Dispatchers.IO) {
            require(eligibleCandidateIds.distinct().size == eligibleCandidateIds.size) { "candidate ids must be unique" }
            val endpoint = BuildConfig.AI_GATEWAY_URL.trim().trimEnd('/')
            require(endpoint.isNotEmpty()) { "AI gateway is not configured" }

            val requestId = UUID.randomUUID().toString()
            val connection = (URL("$endpoint/v1/ai/next-action").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-store")
                setRequestProperty("X-Request-Id", requestId)
            }

            try {
                val body = JSONObject().apply {
                    put("schemaVersion", "1")
                    put("context", JSONObject().apply {
                        putOptional("nowIso", request.context.nowIso)
                        putOptional("activeActivityId", request.context.activeActivityId)
                        put("minimalRouteFacts", JSONArray(request.context.minimalRouteFacts))
                    })
                    put("candidates", JSONArray(eligibleCandidateIds))
                }
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val responseBody = stream?.let {
                    BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() }
                }.orEmpty()
                if (status !in 200..299) error("AI gateway returned HTTP $status [requestId=$requestId]")
                require(responseBody.isNotBlank()) { "AI gateway returned an empty next-action response [requestId=$requestId]" }

                val root = JSONObject(responseBody)
                val result = root.getJSONObject("result")
                val candidateId = result.optString("recommendedAction").takeIf { it.isNotBlank() && it != "null" }
                require(candidateId == null || candidateId in eligibleCandidateIds) {
                    "Gateway recommended a non-eligible candidate [requestId=$requestId]"
                }
                val alternatives = result.optJSONArray("alternatives")?.toStringList().orEmpty()
                require(alternatives.size <= 2) { "Gateway returned too many alternatives [requestId=$requestId]" }
                require(alternatives.all { it in eligibleCandidateIds }) {
                    "Gateway returned a non-eligible alternative [requestId=$requestId]"
                }

                RemoteNextActionRecommendation(
                    candidateId = candidateId,
                    alternatives = alternatives,
                    reason = result.getString("reason"),
                    confidence = result.optString("confidence", "LOW"),
                    requestId = root.optString("requestId", requestId),
                    model = root.optString("model"),
                )
            } finally {
                connection.disconnect()
            }
        }

    private fun JSONObject.putOptional(key: String, value: String?) {
        if (!value.isNullOrBlank()) put(key, value)
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) add(getString(index))
    }
}
