package com.superplanner.app.domain.ai

import org.json.JSONObject
import org.junit.Assert.assertThrows
import org.junit.Test

class AiGatewayProposalValidatorTest {
    @Test
    fun create_activity_requires_confirmation() {
        assertThrows(IllegalArgumentException::class.java) {
            AiGatewayProposalValidator.validate(createProposal(requiresConfirmation = false))
        }
    }

    @Test
    fun create_activity_rejects_start_time_without_date() {
        val proposal = createProposal().apply {
            getJSONObject("payload").put("startTime", "18:00")
        }

        assertThrows(IllegalArgumentException::class.java) {
            AiGatewayProposalValidator.validate(proposal)
        }
    }

    @Test
    fun create_activity_accepts_explicit_date_and_start_time() {
        val proposal = createProposal().apply {
            getJSONObject("payload").put("date", "2026-09-14")
            getJSONObject("payload").put("startTime", "18:00")
        }

        AiGatewayProposalValidator.validate(proposal)
    }

    @Test
    fun create_activity_rejects_zero_or_excessive_duration() {
        val zero = createProposal().apply { getJSONObject("payload").put("durationMinutes", 0) }
        val excessive = createProposal().apply { getJSONObject("payload").put("durationMinutes", 1441) }

        assertThrows(IllegalArgumentException::class.java) {
            AiGatewayProposalValidator.validate(zero)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AiGatewayProposalValidator.validate(excessive)
        }
    }

    @Test
    fun plan_changes_require_confirmation() {
        val proposal = JSONObject("""
            {
              "schemaVersion":"1",
              "commandType":"REORGANIZE_DAY",
              "explanation":"Reorganizar o dia",
              "requiresConfirmation":false,
              "payload":{"message":null,"activityId":"a1","evidence":[],"fields":[],"delayMinutes":30,"title":null,"durationMinutes":null,"date":null,"startTime":null,"priority":null,"energy":null}
            }
        """.trimIndent())

        assertThrows(IllegalArgumentException::class.java) {
            AiGatewayProposalValidator.validate(proposal)
        }
    }

    private fun createProposal(requiresConfirmation: Boolean = true): JSONObject = JSONObject("""
        {
          "schemaVersion":"1",
          "commandType":"CREATE_ACTIVITY_DRAFT",
          "explanation":"Criar atividade",
          "requiresConfirmation":$requiresConfirmation,
          "payload":{"message":null,"activityId":null,"evidence":[],"fields":[],"delayMinutes":null,"title":"Estudar francês","durationMinutes":60,"date":null,"startTime":null,"priority":"IMPORTANT","energy":null}
        }
    """.trimIndent())
}
