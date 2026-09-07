package com.gpsdavida.app.domain.model

import java.time.Duration
import java.time.LocalDate

enum class WeeklyActivityKind {
    EVENT,
    TASK,
    HABIT,
    ROUTINE,
}

data class WeeklyActivity(
    val date: LocalDate,
    val title: String,
    val kind: WeeklyActivityKind,
    val instance: ActivityInstance,
    val goalId: GoalId? = null,
)

data class WeeklyDaySummary(
    val date: LocalDate,
    val activities: List<WeeklyActivity>,
    val plannedDuration: Duration,
    val actualDuration: Duration,
    val plannedCount: Int,
    val completedCount: Int,
    val conflictCount: Int = 0,
) {
    val completionRatio: Double
        get() = if (plannedCount == 0) 0.0 else completedCount.toDouble() / plannedCount
}

data class WeeklyPlanning(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<WeeklyDaySummary>,
) {
    val activities: List<WeeklyActivity>
        get() = days.flatMap { it.activities }

    val plannedDuration: Duration
        get() = days.fold(Duration.ZERO) { total, day -> total.plus(day.plannedDuration) }

    val actualDuration: Duration
        get() = days.fold(Duration.ZERO) { total, day -> total.plus(day.actualDuration) }
}
