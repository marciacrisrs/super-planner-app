package com.superplanner.app.domain.ai

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGatewayProtocolTest {
    @Test
    fun request_contains_contract_version_and_only_minimal_context() {
        val request = AiRequest(
            message = "Preciso estudar francês por uma hora amanhã às 18h.",
            context = AiContext(
                nowIso = "2026-09-13T21:00:00-03:00",
                activeActivityId = "activity-123",
                minimalRouteFacts = listOf("fact.next=Estudar", "fact.availability=18:00-20:00"),
            ),
        )

        val json = AiGatewayProtocol.buildRequestBody(request)
        val context = json.getJSONObject("context")

        assertEquals("1", json.getString("schemaVersion"))
        assertEquals(request.message, json.getString("message"))
        assertEquals("2026-09-13T21:00:00-03:00", context.getString("nowIso"))
        assertEquals("activity-123", context.getString("activeActivityId"))
        assertEquals(2, context.getJSONArray("minimalRouteFacts").length())
        assertEquals(setOf("schemaVersion", "message", "context"), json.keySet())
        assertEquals(setOf("nowIso", "activeActivityId", "minimalRouteFacts"), context.keySet())
        assertFalse(json.toString().contains("password", ignoreCase = true))
        assertFalse(json.toString().contains("api_key", ignoreCase = true))
    }

    @Test
    fun request_omits_optional_context_fields_when_absent() {
        val json = AiGatewayProtocol.buildRequestBody(
            AiRequest(message = "Organizar meu dia"),
        )
        val context = json.getJSONObject("context")

        assertNull(context.optString("nowIso").takeIf(String::isNotBlank))
        assertNull(context.optString("activeActivityId").takeIf(String::isNotBlank))
        assertTrue(context.getJSONArray("minimalRouteFacts").length() == 0)
    }

    @Test
    fun structured_error_preserves_code_message_and_request_id() {
        val error = AiGatewayProtocol.parseError(
            """
            {"error":"schema_version_unsupported","message":"Expected version 1","requestId":"req-42"}
            """.trimIndent(),
        )

        assertEquals("schema_version_unsupported", error.code)
        assertEquals("Expected version 1", error.message)
        assertEquals("req-42", error.requestId)
        assertEquals(
            "schema_version_unsupported: Expected version 1 [requestId=req-42]",
            error.toString(),
        )
    }

    @Test
    fun malformed_error_gets_safe_fallback_and_keeps_request_id() {
        val error = AiGatewayProtocol.parseError("not-json", "req-99")

        assertEquals("ai_gateway_error", error.code)
        assertEquals("not-json", error.message)
        assertEquals("req-99", error.requestId)
    }

    @Test
    fun response_schema_version_is_a_required_client_contract() {
        val valid = JSONObject("""
            {"schemaVersion":"1","commandType":"MISSING_INFORMATION"}
        """)
        val invalid = JSONObject("""
            {"schemaVersion":"2","commandType":"MISSING_INFORMATION"}
        """)

        assertEquals(AI_PROPOSAL_SCHEMA_VERSION, valid.getString("schemaVersion"))
        assertTrue(invalid.getString("schemaVersion") != AI_PROPOSAL_SCHEMA_VERSION)
    }
}
