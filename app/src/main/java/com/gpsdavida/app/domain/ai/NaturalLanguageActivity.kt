package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import java.time.Duration
import java.time.Instant

/** Structured interpretation of natural-language activity input. Nothing is persisted here. */
data class NaturalLanguageActivityDraft(
    val title: String,
    val plannedDuration: Duration?,
    val start: Instant?,
    val due: Instant?,
    val priority: Priority,
    val energy: Energy?,
    val missingFields: Set<ActivityField>,
    val interpretationNotes: List<String>,
) {
    val readyToPreview: Boolean = title.isNotBlank() && plannedDuration != null
}

enum class ActivityField {
    DURATION,
    DATE,
    TIME,
    PRIORITY,
}

data class NaturalLanguageActivityInput(
    val text: String,
    val now: Instant,
)
