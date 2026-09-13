package com.superplanner.app.domain.model

import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanningValueMetricsCalculatorTest {
    @Test
    fun `calculates decision quality metrics without productivity score`() {
        val metrics = PlanningValueMetricsCalculator.calculate(
            listOf(
                PlanningValueEvidence(
                    firstPlanningDuration = Duration.ofMinutes(4),
                    manualRouteInterventions = 2,
                    impossibleConflictCount = 1,
                    automaticallyResolvedDecisionCount = 8,
                    plannedActivityCount = 10,
                    completedPlannedActivityCount = 7,
                    priorityActivityCount = 4,
                    priorityActivityCompletedCount = 4,
                    agoraOpenedCount = 3,
                    recoveryAttemptCount = 2,
                    successfulRecoveryCount = 1,
                ),
            ),
        )

        assertEquals(Duration.ofMinutes(4), metrics.firstPlanningDuration)
        assertEquals(0.8, metrics.automaticDecisionRate, 0.0001)
        assertEquals(0.7, metrics.plannedVsCompletedRate, 0.0001)
        assertEquals(1.0, metrics.priorityPreservationRate, 0.0001)
        assertEquals(1.0, metrics.agoraUseRate, 0.0001)
        assertEquals(0.5, metrics.recoverySuccessRate, 0.0001)
        assertTrue(metrics.avoidsProductivityScore)
    }

    @Test
    fun `does not manufacture rates when denominator has no evidence`() {
        val metrics = PlanningValueMetricsCalculator.calculate(listOf(PlanningValueEvidence()))

        assertNull(metrics.automaticDecisionRate)
        assertNull(metrics.plannedVsCompletedRate)
        assertNull(metrics.priorityPreservationRate)
        assertNull(metrics.recoverySuccessRate)
    }
}
