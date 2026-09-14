package com.superplanner.app.domain.ai

import org.json.JSONArray
import org.json.JSONObject

internal const val AI_PROPOSAL_SCHEMA_VERSION = "1"

internal data class AiGatewayError(
    val code: String,
    val message: String?,
    val requestId: String?,
) {
    override fun toString(): String = buildString {
        append(code)
        message?.takeIf(String::isNotBlank)?.let { append(": ").append(it) }
        requestId?.takeIf(String::isNotBlank)?.let { append(" [requestId=").append(it).append(']') }
    }
}

internal object AiGatewayProtocol {
    fun buildRequestBody(request: AiRequest): JSONObject = JSONObject().apply {
        put("schemaVersion", AI_PROPOSAL_SCHEMA_VERSION)
        put("message", request.message)
        put("context", JSONObject().apply {
            request.context.nowIso?.let { put("nowIso", it) }
            request.context.activeActivityId?.let { put("activeActivityId", it) }
            put("minimalRouteFacts", JSONArray(request.context.minimalRouteFacts))
        })
    }

    fun parseError(responseBody: String, fallbackRequestId: String? = null): AiGatewayError {
        return runCatching {
            val json = JSONObject(responseBody)
            AiGatewayError(
                code = json.optString("error").ifBlank { "ai_gateway_error" },
                message = json.optString("message").takeIf(String::isNotBlank),
                requestId = json.optString("requestId").takeIf(String::isNotBlank) ?: fallbackRequestId,
            )
        }.getOrElse {
            AiGatewayError("ai_gateway_error", responseBody.takeIf(String::isNotBlank), fallbackRequestId)
        }
    }
}
