package com.superplanner.app.domain.planning

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextualNextActionTest {
    private val now = Instant.parse("2026-09-14T13:00:00Z")

    @Test
    fun short_window_rejects_work_that_cannot_fit_with_logistics() {
        val result = ContextualNextActionSelector().select(
            NextActionContext(now, now.plusSeconds(35 * 60), Duration.ofMinutes(35)),
            listOf(
                candidate("long", 40, priority = 5, travel = 10),
                candidate("short", 20, priority = 3, travel = 10),
            ),
        )

        assertEquals("short", result.recommended?.id)
        assertTrue(result.rejected.any { it.candidateId == "long" })
    }

    @Test
    fun conflict_with_upcoming_commitment_is_never_recommended() {
        val commitment = now.plusSeconds(30 * 60)
        val result = ContextualNextActionSelector().select(
            NextActionContext(now, now.plusSeconds(60 * 60), Duration.ofHours(1), commitment),
            listOf(candidate("conflicting", 25, priority = 10, preparation = 10)),
        )

        assertEquals(null, result.recommended)
        assertTrue(result.rejected.single().reason.contains("próximo compromisso"))
    }

    @Test
    fun priority_ranks_feasible_options_and_limits_alternatives() {
        val result = ContextualNextActionSelector().select(
            NextActionContext(now, now.plusSeconds(2 * 60 * 60), Duration.ofHours(2)),
            listOf(
                candidate("low", 20, priority = 1),
                candidate("high", 30, priority = 5),
                candidate("medium", 15, priority = 3),
                candidate("also", 10, priority = 2),
            ),
        )

        assertEquals("high", result.recommended?.id)
        assertEquals(listOf("medium", "also"), result.alternatives.map { it.id })
    }

    @Test
    fun dependency_and_availability_are_domain_constraints() {
        val result = ContextualNextActionSelector().select(
            NextActionContext(now, now.plusSeconds(60 * 60), Duration.ofHours(1)),
            listOf(
                candidate("blocked", 20, priority = 10).copy(dependencySatisfied = false),
                candidate("later", 20, priority = 9).copy(availableFrom = now.plusSeconds(10 * 60)),
            ),
        )

        assertEquals(null, result.recommended)
        assertEquals(2, result.rejected.size)
    }

    private fun candidate(
        id: String,
        minutes: Long,
        priority: Int,
        preparation: Long = 0,
        travel: Long = 0,
    ) = NextActionCandidate(
        id = id,
        title = id,
        duration = Duration.ofMinutes(minutes),
        priorityWeight = priority,
        availableFrom = now,
        preparation = Duration.ofMinutes(preparation),
        travel = Duration.ofMinutes(travel),
    )
}
