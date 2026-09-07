package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.domain.model.RecurrenceRule
import com.gpsdavida.app.domain.model.RecurrenceUnit
import java.time.LocalDate
import javax.inject.Inject

/** Calculates recurrence occurrences without materializing future records. */
class RecurrenceCalculator @Inject constructor() {
    fun occursOn(rule: RecurrenceRule, date: LocalDate): Boolean {
        if (date.isBefore(rule.startDate)) return false
        if (rule.endDate != null && date.isAfter(rule.endDate)) return false

        return when (rule.unit) {
            RecurrenceUnit.DAY -> daysBetween(rule.startDate, date) % rule.interval == 0L
            RecurrenceUnit.WEEK -> weeksBetween(rule.startDate, date) % rule.interval == 0L && matchesDay(rule, date)
            RecurrenceUnit.MONTH -> monthsBetween(rule.startDate, date) % rule.interval == 0L && date.dayOfMonth == rule.startDate.dayOfMonth
            RecurrenceUnit.YEAR -> yearsBetween(rule.startDate, date) % rule.interval == 0L &&
                date.month == rule.startDate.month && date.dayOfMonth == rule.startDate.dayOfMonth
        }
    }

    fun nextOccurrence(rule: RecurrenceRule, after: LocalDate): LocalDate? {
        var candidate = when (rule.unit) {
            RecurrenceUnit.DAY -> after.plusDays(1)
            RecurrenceUnit.WEEK -> after.plusDays(1)
            RecurrenceUnit.MONTH -> after.plusDays(1)
            RecurrenceUnit.YEAR -> after.plusDays(1)
        }.coerceAtLeast(rule.startDate)

        repeat(366 * 20) {
            if (rule.endDate != null && candidate.isAfter(rule.endDate)) return null
            if (occursOn(rule, candidate)) return candidate
            candidate = candidate.plusDays(1)
        }
        return null
    }

    fun occurrences(rule: RecurrenceRule, from: LocalDate, to: LocalDate): List<LocalDate> {
        if (to.isBefore(from)) return emptyList()
        val start = maxOf(from, rule.startDate)
        val end = minOf(to, rule.endDate ?: to)
        if (end.isBefore(start)) return emptyList()
        return generateSequence(start) { date -> date.takeIf { it.isBefore(end) }?.plusDays(1) }
            .filter { occursOn(rule, it) }
            .toList()
    }

    private fun matchesDay(rule: RecurrenceRule, date: LocalDate): Boolean =
        rule.daysOfWeek.isEmpty() || date.dayOfWeek in rule.daysOfWeek

    private fun daysBetween(start: LocalDate, end: LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(start, end)

    private fun weeksBetween(start: LocalDate, end: LocalDate): Long =
        daysBetween(start, end) / 7L

    private fun monthsBetween(start: LocalDate, end: LocalDate): Long =
        java.time.temporal.ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1))

    private fun yearsBetween(start: LocalDate, end: LocalDate): Long =
        java.time.temporal.ChronoUnit.YEARS.between(start.withDayOfYear(1), end.withDayOfYear(1))
}
