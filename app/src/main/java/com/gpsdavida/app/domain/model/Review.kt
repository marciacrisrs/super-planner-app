package com.superplanner.app.domain.model

import java.time.LocalDate

data class ReviewSuggestion(
    val id: String,
    val title: String,
    val explanation: String,
    val actionLabel: String,
)

data class DailyReview(
    val date: LocalDate,
    val priorityTitle: String?,
    val plannedCount: Int,
    val completedCount: Int,
    val changed: List<String>,
    val toReorganize: List<String>,
    val suggestions: List<ReviewSuggestion>,
)

data class WeeklyReview(
    val advancedPriorities: List<String>,
    val repeatedlyDeferred: List<String>,
    val nextWeekChanges: List<String>,
    val neglectedAreas: List<String>,
    val suggestions: List<ReviewSuggestion>,
)
