package com.superplanner.app.domain.model

import java.time.LocalDate

/** A small, confirmable planning proposal derived from an intent. */
data class IntentPlanProposal(
    val intent: String,
    val goal: Goal,
    val milestones: List<Milestone>,
    val nextActivities: List<IntentActivityProposal>,
    val requiresConfirmation: Boolean = true,
)

data class IntentActivityProposal(
    val title: String,
    val targetDate: LocalDate? = null,
    val durationMinutes: Long? = null,
    val milestoneId: String? = null,
)
