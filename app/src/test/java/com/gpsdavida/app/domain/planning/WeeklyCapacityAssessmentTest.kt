package com.superplanner.app.domain.planning

import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyCapacityAssessmentTest {
    private val estimator = WeeklyCapacityEstimator()

    @Test
    fun low_load_is_sustainable_and_keeps_recovery_out_of_capacity() {
        val result = estimator.estimate(
            CapacityEstimateInput(
                date = "2026-09-14",
                availableWindow = Duration.ofHours(12),
                fixedCommitments = Duration.ofHours(2),
                work = Duration.ofHours(4),
                sleepAndRecovery = Duration.ofHours(2),
                logistics = Duration.ofHours(1),
                preparation = Duration.ofMinutes(30),
                desiredActivities = Duration.ofHours(1),
            ),
        )

        assertEquals(CapacityLoad.SUSTAINABLE, result.load)
        assertTrue(result.reasons.any { it.contains("recuperação") })
        assertTrue(result.schedulableCapacity < Duration.ofHours(4))
    }

    @Test
    fun medium_load_is_tight_before_it_becomes_overload() {
        val result = estimator.estimate(
            CapacityEstimateInput(
                date = "2026-09-15",
                availableWindow = Duration.ofHours(10),
                fixedCommitments = Duration.ofHours(1),
                work = Duration.ofHours(3),
                sleepAndRecovery = Duration.ofHours(1),
                logistics = Duration.ofHours(1),
                preparation = Duration.ofMinutes(30),
                desiredActivities = Duration.ofHours(2),
            ),
        )

        assertEquals(CapacityLoad.TIGHT, result.load)
    }

    @Test
    fun high_load_is_over_capacity_and_history_makes_estimate_more_conservative() {
        val result = estimator.estimate(
            CapacityEstimateInput(
                date = "2026-09-16",
                availableWindow = Duration.ofHours(10),
                fixedCommitments = Duration.ofHours(1),
                work = Duration.ofHours(3),
                sleepAndRecovery = Duration.ofHours(1),
                logistics = Duration.ofHours(1),
                preparation = Duration.ofHours(1),
                desiredActivities = Duration.ofHours(4),
                historicalPlanned = Duration.ofHours(2),
                historicalActual = Duration.ofHours(3),
            ),
        )

        assertEquals(CapacityLoad.OVER_CAPACITY, result.load)
        assertTrue(result.reasons.any { it.contains("histórico") })
    }

    @Test
    fun weekly_assessment_aggregates_daily_capacity_and_tradeoffs() {
        val result = estimator.estimateWeek(
            listOf(
                CapacityEstimateInput("2026-09-14", Duration.ofHours(10), work = Duration.ofHours(4), desiredActivities = Duration.ofHours(1)),
                CapacityEstimateInput("2026-09-15", Duration.ofHours(10), work = Duration.ofHours(4), desiredActivities = Duration.ofHours(5)),
            ),
        )

        assertEquals(2, result.days.size)
        assertEquals(CapacityLoad.TIGHT, result.load)
        assertTrue(result.totalCapacity > Duration.ZERO)
    }
}
