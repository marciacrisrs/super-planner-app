package com.superplanner.app.domain.ai

import org.json.JSONObject

/** Client-side semantic guardrail. The gateway interprets; the app still rejects unsafe proposals. */
internal object AiGatewayProposalValidator {
    fun validate(json: JSONObject) {
        require(json.optString("schemaVersion") == AI_PROPOSAL_SCHEMA_VERSION) {
            "Unsupported AI proposal schema"
        }

        val commandType = json.optString("commandType")
        require(commandType.isNotBlank()) { "AI proposal is missing commandType" }

        val explanation = json.optString("explanation").trim()
        require(explanation.isNotBlank()) { "AI proposal is missing explanation" }

        val payload = json.optJSONObject("payload")
            ?: error("AI proposal is missing payload")
        val requiresConfirmation = json.optBoolean("requiresConfirmation", false)

        when (commandType) {
            "CREATE_ACTIVITY_DRAFT" -> {
                require(requiresConfirmation) { "Creating an activity always requires confirmation" }
                validateCreateActivity(payload)
            }
            "REORGANIZE_DAY", "RECALCULATE_ROUTE" -> {
                require(requiresConfirmation) { "Changing the plan always requires confirmation" }
            }
            "EXPLAIN_NEXT_ACTIVITY", "MISSING_INFORMATION" -> Unit
            else -> error("Unsupported AI command: $commandType")
        }
    }

    private fun validateCreateActivity(payload: JSONObject) {
        require(payload.optString("title").trim().isNotBlank()) {
            "CREATE_ACTIVITY_DRAFT requires a title"
        }

        val duration = payload.optInt("durationMinutes", 0)
        require(duration in 1..1440) {
            "CREATE_ACTIVITY_DRAFT requires durationMinutes between 1 and 1440"
        }

        val date = payload.optString("date").takeIf(String::isNotBlank)
        val startTime = payload.optString("startTime").takeIf(String::isNotBlank)

        date?.let {
            require(runCatching { java.time.LocalDate.parse(it) }.isSuccess) {
                "CREATE_ACTIVITY_DRAFT date must be ISO-8601"
            }
        }
        startTime?.let {
            require(date != null) {
                "CREATE_ACTIVITY_DRAFT startTime requires date"
            }
            require(runCatching { java.time.LocalTime.parse(it) }.isSuccess) {
                "CREATE_ACTIVITY_DRAFT startTime must be HH:mm"
            }
        }

        payload.optString("priority").takeIf(String::isNotBlank)?.let {
            require(it in setOf("REQUIRED", "IMPORTANT", "DESIRABLE", "LEISURE")) {
                "Unknown AI priority"
            }
        }
        payload.optString("energy").takeIf(String::isNotBlank)?.let {
            require(it in setOf("LOW", "MEDIUM", "HIGH")) {
                "Unknown AI energy"
            }
        }
    }
}
