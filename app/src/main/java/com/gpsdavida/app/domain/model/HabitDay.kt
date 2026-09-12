package com.superplanner.app.domain.model

import java.time.Instant
import java.time.LocalDate

data class HabitDay(
    val habit: Habit,
    val date: LocalDate,
    val completedAt: Instant? = null,
) {
    val isDone: Boolean get() = completedAt != null
}
