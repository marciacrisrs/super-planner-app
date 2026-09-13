package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.PlanningImprovementType
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestPlanningImprovementsTest {
    private val plannedStart = Instant.parse("2026-09-13T09:00:00Z")
    private val activityId = ActivityInstanceId("study")

    @Test
    fun `suggests longer typical duration only with repeated evidence`() {
        val executions = (1..4).map { index ->
            ActivityExecution(
                activityInstanceId = activityId,
                status = ActivityStatus.DONE,
                planned = TimeRange(plannedStart.plusSeconds(index.toLong() * 86_400), plannedStart.plusSeconds(index.toLong() * 86_400 + 1_800)),
                actualStart = plannedStart.plusSeconds(index.toLong() * 86_400),
                actual = TimeRange(plannedStart.plusSeconds(index.toLong() * 86_400), plannedStart.plusSeconds(index.toLong() * 86_400 + 2_700)),
            )
        }

        val suggestions = SuggestPlanningImprovements()(executions)
        val duration = suggestions.single { it.type == PlanningImprovementType.IncreaseTypicalDuration }

        assertEquals(4, duration.sampleSize)
        assertEquals(45L, duration.suggestedDuration!!.toMinutes())
        assertTrue(duration.evidence.single().matchingObservationCount >= 3)
    }

    @Test
    fun `does not make strong suggestion from small sample`() {
        val executions = listOf(
            ActivityExecution(
                activityInstanceId = activityId,
                status = ActivityStatus.DONE,
                planned = TimeRange(plannedStart, plannedStart.plusSeconds(1_800)),
                actualStart = plannedStart,
                actual = TimeRange(plannedStart, plannedStart.plusSeconds(2_700)),
            ),
            ActivityExecution(
                activityInstanceId = activityId,
                status = ActivityStatus.DONE,
                planned = TimeRange(plannedStart.plusSeconds(86_400), plannedStart.plusSeconds(86_400 + 1_800)),
                actualStart = plannedStart.plusSeconds(86_400),
                actual = TimeRange(plannedStart.plusSeconds(86_400), plannedStart.plusSeconds(86_400 + 2_700)),
            ),
        )

        assertTrue(SuggestPlanningImprovements()(executions).isEmpty())
    }

    @Test
    fun `suggests repeated deferral with evidence and never mutates execution history`() {
        val executions = (1..4).map { index ->
            ActivityExecution(
                activityInstanceId = activityId,
                status = if (index <= 3) ActivityStatus.DEFERRED else ActivityStatus.DONE,
                planned = TimeRange(plannedStart.plusSeconds(index.toLong() * 86_400), plannedStart.plusSeconds(index.toLong() * 86_400 + 1_800)),
                actual = if (index == 4) TimeRange(plannedStart.plusSeconds(4 * 86_400L), plannedStart.plusSeconds(4 * 86_400L + 1_800)) else null,
            )
        }
        val originalStatuses = executions.map { it.status }

        val suggestion = SuggestPlanningImprovements()(executions)
            .single { it.type == PlanningImprovementType.RepeatedDeferral }

        assertEquals(3, suggestion.evidence.single().matchingObservationCount)
        assertEquals(originalStatuses, executions.map { it.status })
    }
}
