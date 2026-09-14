package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.json.JSONObject

/** Maps a validated Gateway proposal into the Planner's domain command. */
internal object AiGatewayProposalMapper {
    fun mapCreateActivityDraft(payload: JSONObject, request: AiRequest): NaturalLanguageActivityDraft {
        val today = request.context.nowIso?.take(10)?.let(LocalDate::parse) ?: LocalDate.now()
        val title = payload.optString("title").trim()
        val durationMinutes = payload.optInt("durationMinutes", 0)
        val date = payload.optString("date").takeIf(String::isNotBlank)?.let(LocalDate::parse)
        val startTime = payload.optString("startTime").takeIf(String::isNotBlank)?.let(LocalTime::parse)
        val priority = payload.optString("priority")
            .takeIf(String::isNotBlank)
            ?.let { Priority.valueOf(it) }
            ?: Priority.IMPORTANT
        val energy = payload.optString("energy")
            .takeIf(String::isNotBlank)
            ?.let { Energy.valueOf(it) }

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
