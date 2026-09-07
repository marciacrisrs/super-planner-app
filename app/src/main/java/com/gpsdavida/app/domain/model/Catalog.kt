package com.gpsdavida.app.domain.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class Event(
    val id: EventId,
    val title: String,
    val range: TimeRange,
    val recurrenceDays: Set<DayOfWeek> = emptySet(),
    val recurrenceInterval: Int = 1,
    val recurrenceUnit: RecurrenceUnit? = null,
    val recurrenceEndDate: LocalDate? = null,
    val priority: Priority = Priority.REQUIRED,
    val energy: Energy? = null,
    val goalId: GoalId? = null,
) {
    val flexibility: Flexibility get() = Flexibility.FIXED

    fun occursOn(date: LocalDate, zone: ZoneId): Boolean {
        val startDate = range.start.atZone(zone).toLocalDate()
        val rule = recurrenceUnit
        if (rule == null) {
            if (recurrenceDays.isEmpty()) return date == startDate
            return !date.isBefore(startDate) && date.dayOfWeek in recurrenceDays
        }
        val recurrence = RecurrenceRule(
            startDate = startDate,
            endDate = recurrenceEndDate,
            interval = recurrenceInterval,
            unit = rule,
            daysOfWeek = recurrenceDays,
        )
        return RecurrenceCalculatorBridge.occursOn(recurrence, date)
    }
}

data class Task(
    val id: TaskId,
    val title: String,
    val plannedDuration: Duration,
    val priority: Priority,
    val due: Instant? = null,
    val completedAt: Instant? = null,
    val energy: Energy? = null,
    val goalId: GoalId? = null,
) {
    val flexibility: Flexibility get() = Flexibility.FLEXIBLE

    val isDone: Boolean get() = completedAt != null

    fun belongsOnDay(date: LocalDate, zone: ZoneId): Boolean {
        if (completedAt != null) return completedAt.atZone(zone).toLocalDate() == date
        val dueDate = due?.atZone(zone)?.toLocalDate() ?: return false
        return !dueDate.isAfter(date)
    }
}

data class Habit(
    val id: HabitId,
    val title: String,
    val plannedDuration: Duration,
    val daysOfWeek: Set<DayOfWeek>,
    val window: LocalTimeWindow? = null,
    val priority: Priority = Priority.IMPORTANT,
    val energy: Energy? = null,
    val goalId: GoalId? = null,
) {
    val flexibility: Flexibility get() = Flexibility.FLEXIBLE
    fun occursOn(date: LocalDate): Boolean = daysOfWeek.isEmpty() || date.dayOfWeek in daysOfWeek
}

data class RoutineStep(
    val id: RoutineStepId,
    val title: String,
    val plannedDuration: Duration,
    val order: Int,
)

data class Routine(
    val id: RoutineId,
    val title: String,
    val steps: List<RoutineStep>,
    val startTime: LocalTime? = null,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val priority: Priority = Priority.IMPORTANT,
    val energy: Energy? = null,
    val goalId: GoalId? = null,
) {
    val flexibility: Flexibility get() = Flexibility.FLEXIBLE
}

data class Availability(
    val id: AvailabilityId,
    val dayOfWeek: DayOfWeek,
    val window: LocalTimeWindow,
    val kind: AvailabilityKind,
)

data class Goal(
    val id: GoalId,
    val title: String,
)

data class Dependency(
    val id: DependencyId,
    val predecessor: ActivitySource,
    val successor: ActivitySource,
)

/** Small pure bridge so domain models remain free from dependency injection. */
internal object RecurrenceCalculatorBridge {
    fun occursOn(rule: RecurrenceRule, date: LocalDate): Boolean {
        if (date.isBefore(rule.startDate)) return false
        if (rule.endDate != null && date.isAfter(rule.endDate)) return false
        val days = java.time.temporal.ChronoUnit.DAYS.between(rule.startDate, date)
        return when (rule.unit) {
            RecurrenceUnit.DAY -> days % rule.interval == 0L
            RecurrenceUnit.WEEK -> days / 7L % rule.interval == 0L &&
                (rule.daysOfWeek.isEmpty() || date.dayOfWeek in rule.daysOfWeek)
            RecurrenceUnit.MONTH -> java.time.temporal.ChronoUnit.MONTHS.between(
                rule.startDate.withDayOfMonth(1), date.withDayOfMonth(1),
            ) % rule.interval == 0L && date.dayOfMonth == rule.startDate.dayOfMonth
            RecurrenceUnit.YEAR -> java.time.temporal.ChronoUnit.YEARS.between(
                rule.startDate.withDayOfYear(1), date.withDayOfYear(1),
            ) % rule.interval == 0L && date.month == rule.startDate.month && date.dayOfMonth == rule.startDate.dayOfMonth
        }
    }
}
