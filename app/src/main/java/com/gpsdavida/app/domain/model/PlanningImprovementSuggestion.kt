package com.superplanner.app.domain.model

import java.time.Duration

sealed interface PlanningImprovementType {
    data object IncreaseTypicalDuration : PlanningImprovementType
    data object RepeatedDeferral : PlanningImprovementType
}

data class PlanningEvidence(
    val activityInstanceId: ActivityInstanceId,
    val observationCount: Int,
    val matchingObservationCount: Int,
    val observedValues: List<String>,
)

data class PlanningImprovementSuggestion(
    val type: PlanningImprovementType,
    val title: String,
    val explanation: String,
    val evidence: List<PlanningEvidence>,
    val sampleSize: Int,
    val suggestedDuration: Duration? = null,
)
