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
    fun proposal_validator_accepts_valid_fixed_start_draft() {
        val proposal = JSONObject(
            """
            {
              "schemaVersion":"1",
              "commandType":"CREATE_ACTIVITY_DRAFT",
              "explanation":"Você informou o horário explicitamente.",
              "requiresConfirmation":true,
              "payload":{
                "title":"Estudar francês",
                "durationMinutes":60,
                "date":"2026-09-14",
                "startTime":"18:00",
                "priority":"IMPORTANT",
                "energy":"HIGH"
              }
            }
            """.trimIndent(),
        )

        AiGatewayProposalValidator.validate(proposal)
        assertEquals("18:00", proposal.getJSONObject("payload").getString("startTime"))
    }

    @Test
    fun proposal_validator_rejects_fixed_start_without_date() {
        val proposal = validCreateProposal().apply {
            getJSONObject("payload").remove("date")
        }

        assertRejected(proposal, "startTime requires date")
    }

    @Test
    fun proposal_validator_rejects_create_without_confirmation() {
        val proposal = validCreateProposal().apply {
            put("requiresConfirmation", false)
        }

        assertRejected(proposal, "always requires confirmation")
    }

    @Test
    fun proposal_validator_rejects_invalid_duration() {
        val proposal = validCreateProposal().apply {
            getJSONObject("payload").put("durationMinutes", 0)
        }

        assertRejected(proposal, "durationMinutes")
    }

    @Test
    fun proposal_validator_rejects_unknown_schema() {
        val proposal = validCreateProposal().apply {
            put("schemaVersion", "2")
        }

        assertRejected(proposal, "Unsupported AI proposal schema")
    }

    @Test
    fun proposal_validator_requires_confirmation_for_plan_changes() {
        val proposal = JSONObject(
            """
            {
              "schemaVersion":"1",
              "commandType":"REORGANIZE_DAY",
              "explanation":"Atrasar a atividade.",
              "requiresConfirmation":false,
              "payload":{}
            }
            """.trimIndent(),
        )

        assertRejected(proposal, "always requires confirmation")
    }

    private fun validCreateProposal(): JSONObject = JSONObject(
        """
        {
          "schemaVersion":"1",
          "commandType":"CREATE_ACTIVITY_DRAFT",
          "explanation":"Criar rascunho para confirmação.",
          "requiresConfirmation":true,
          "payload":{
            "title":"Estudar francês",
            "durationMinutes":60,
            "date":"2026-09-14",
            "startTime":"18:00",
            "priority":"IMPORTANT",
            "energy":null
          }
        }
        """.trimIndent(),
    )

    private fun assertRejected(proposal: JSONObject, expectedMessage: String) {
        val error = runCatching { AiGatewayProposalValidator.validate(proposal) }.exceptionOrNull()
        assertTrue("Expected validator rejection", error is IllegalArgumentException)
        assertTrue(error?.message.orEmpty().contains(expectedMessage))
    }
}
