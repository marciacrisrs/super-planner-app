package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.RecurrenceRule
import com.superplanner.app.domain.model.RecurrenceUnit
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/** Structured interpretation of natural-language activity input. Nothing is persisted from this type alone. */
data class NaturalLanguageActivityDraft(
    val sourceText: String,
    val title: String,
    val plannedDuration: Duration?,
    val date: LocalDate?,
    val startTime: LocalTime?,
    val recurrence: RecurrenceRule?,
    val priority: Priority,
    val energy: Energy?,
    val missingFields: Set<MissingActivityField> = emptySet(),
)

enum class MissingActivityField {
    TITLE,
    DURATION,
}

object NaturalLanguageActivityParser {
    fun parse(text: String, today: LocalDate): NaturalLanguageActivityDraft {
        val normalized = text.trim().replace(Regex("\\s+"), " ")
        val duration = parseDuration(normalized)
        val date = when {
            normalized.contains("depois de amanhã", ignoreCase = true) -> today.plusDays(2)
            normalized.contains("amanhã", ignoreCase = true) -> today.plusDays(1)
            normalized.contains("hoje", ignoreCase = true) -> today
            else -> null
        }
        val startTime = parseTime(normalized)
        val recurrence = parseRecurrence(normalized, today)
        val priority = when {
            normalized.contains("urgente", ignoreCase = true) -> Priority.REQUIRED
            normalized.contains("importante", ignoreCase = true) -> Priority.IMPORTANT
            else -> Priority.IMPORTANT
        }
        val energy = when {
            normalized.contains("leve", ignoreCase = true) -> Energy.LOW
            normalized.contains("cansada", ignoreCase = true) -> Energy.LOW
            normalized.contains("foco", ignoreCase = true) -> Energy.HIGH
            else -> null
        }

        val title = extractTitle(normalized)
        val missing = buildSet {
            if (title.isBlank()) add(MissingActivityField.TITLE)
            if (duration == null) add(MissingActivityField.DURATION)
        }

        return NaturalLanguageActivityDraft(
            sourceText = text,
            title = title,
            plannedDuration = duration,
            date = date,
            startTime = startTime,
            recurrence = recurrence,
            priority = priority,
            energy = energy,
            missingFields = missing,
        )
    }

    private fun parseDuration(text: String): Duration? {
        Regex("(?i)(\\d+)\\s*(?:h|hora|horas)").find(text)?.let {
            return Duration.ofHours(it.groupValues[1].toLong())
        }
        Regex("(?i)(\\d+)\\s*(?:min|minuto|minutos)").find(text)?.let {
            return Duration.ofMinutes(it.groupValues[1].toLong())
        }
        if (text.contains("uma hora", ignoreCase = true)) return Duration.ofHours(1)
        if (text.contains("meia hora", ignoreCase = true)) return Duration.ofMinutes(30)
        return null
    }

    private fun parseTime(text: String): LocalTime? {
        Regex("(?i)\\b(?:às|as)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*h?").find(text)?.let {
            val hour = it.groupValues[1].toInt()
            val minute = it.groupValues[2].takeIf(String::isNotBlank)?.toInt() ?: 0
            if (hour in 0..23 && minute in 0..59) return LocalTime.of(hour, minute)
        }
        return null
    }

    private fun parseRecurrence(text: String, today: LocalDate): RecurrenceRule? {
        if (text.contains("todo dia", ignoreCase = true) || text.contains("todos os dias", ignoreCase = true)) {
            return RecurrenceRule(startDate = today, interval = 1, unit = RecurrenceUnit.DAY)
        }
        if (text.contains("segunda a sexta", ignoreCase = true)) {
            return RecurrenceRule(
                startDate = today,
                interval = 1,
                unit = RecurrenceUnit.WEEK,
                daysOfWeek = setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                ),
            )
        }
        return null
    }

    private fun extractTitle(text: String): String {
        var title = text
        listOf(
            Regex("(?i)\\bamanhã\\b"),
            Regex("(?i)\\bdepois de amanhã\\b"),
            Regex("(?i)\\bhoje\\b"),
            Regex("(?i)\\bpreciso\\s+(?:estudar|fazer|trabalhar|ir|resolver|lembrar de)?\\b"),
            Regex("(?i)\\bpor\\s+(?:uma\\s+hora|meia\\s+hora|\\d+\\s*(?:h|hora|horas|min|minuto|minutos))\\b"),
            Regex("(?i)\\b\\d+\\s*(?:h|hora|horas|min|minuto|minutos)\\b"),
            Regex("(?i)\\b(?:às|as)\\s*\\d{1,2}(?::\\d{2})?\\s*h?\\b"),
            Regex("(?i)\\bdepois do trabalho\\b"),
            Regex("(?i)\\btodos os dias\\b"),
            Regex("(?i)\\btodo dia\\b"),
            Regex("(?i)\\bsegunda a sexta\\b"),
            Regex("(?i)\\b(?:urgente|importante|leve|cansada|foco)\\b"),
        ).forEach { title = it.replace(title, " ") }

        title = title
            .replace(Regex("(?i)\\b(estudar|fazer|trabalhar|ir|resolver|lembrar)\\s+"), "")
            .replace(Regex("[,.]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .removePrefix("preciso ")
            .trim()

        return title
    }
}
