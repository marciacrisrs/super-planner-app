package com.superplanner.app.domain.model

import java.time.Duration

data class PlanningValueEvidence(
    val firstPlanningDuration: Duration? = null,
    val manualRouteInterventions: Int = 0,
    val impossibleConflictCount: Int = 0,
    val automaticallyResolvedDecisionCount: Int = 0,
    val plannedActivityCount: Int = 0,
    val completedPlannedActivityCount: Int = 0,
    val priorityActivityCount: Int = 0,
    val priorityActivityCompletedCount: Int = 0,
    val agoraOpenedCount: Int = 0,
    val recoveryAttemptCount: Int = 0,
    val successfulRecoveryCount: Int = 0,
)

data class PlanningValueMetrics(
    val firstPlanningDuration: Duration?,
    val averageManualInterventions: Double,
    val averageImpossibleConflicts: Double,
    val automaticDecisionRate: Double?,
    val plannedVsCompletedRate: Double?,
    val priorityPreservationRate: Double?,
    val agoraUseRate: Double?,
    val recoverySuccessRate: Double?,
) {
    /** These metrics intentionally exclude streaks, perfect-day scores and raw usage time. */
    val avoidsProductivityScore: Boolean = true
}

object PlanningValueMetricsCalculator {
    fun calculate(samples: List<PlanningValueEvidence>): PlanningValueMetrics {
        if (samples.isEmpty()) {
            return PlanningValueMetrics(null, 0.0, 0.0, null, null, null, null, null)
        }
        return PlanningValueMetrics(
            firstPlanningDuration = samples.mapNotNull { it.firstPlanningDuration }.minOrNull(),
            averageManualInterventions = samples.map { it.manualRouteInterventions }.average(),
            averageImpossibleConflicts = samples.map { it.impossibleConflictCount }.average(),
            automaticDecisionRate = rateOf(
                numerator = samples.sumOf { it.automaticallyResolvedDecisionCount },
                denominator = samples.sumOf { it.automaticallyResolvedDecisionCount + it.manualRouteInterventions },
            ),
            plannedVsCompletedRate = rateOf(
                numerator = samples.sumOf { it.completedPlannedActivityCount },
                denominator = samples.sumOf { it.plannedActivityCount },
            ),
            priorityPreservationRate = rateOf(
                numerator = samples.sumOf { it.priorityActivityCompletedCount },
                denominator = samples.sumOf { it.priorityActivityCount },
            ),
            agoraUseRate = rateOf(
                numerator = samples.count { it.agoraOpenedCount > 0 },
                denominator = samples.size,
            ),
            recoverySuccessRate = rateOf(
                numerator = samples.sumOf { it.successfulRecoveryCount },
                denominator = samples.sumOf { it.recoveryAttemptCount },
            ),
        )
    }

    private fun rateOf(numerator: Int, denominator: Int): Double? =
        if (denominator == 0) null else numerator.toDouble() / denominator.toDouble()
}
