package com.superplanner.app.domain.model

import java.time.LocalDate

data class Milestone(
    val id: String,
    val title: String,
    val targetDate: LocalDate,
    val goalId: GoalId? = null,
)
