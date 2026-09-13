package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test
import java.time.Instant

class PlanningEngineContractTest {
    private val input = PlanningInput(
        activities = mutableListOf(
            ActivityInstance(
                id = ActivityInstanceId("activity-1"),
                source = ActivitySource.FromTask(TaskId("task-1")),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(
                    Instant.parse("2026-09-13T09:00:00Z"),
                    Instant.parse("2026-09-13T09:30:00Z"),
                ),
            ),
        ),
        context = NextActionContext(now = Instant.parse("2026-09-13T12:00:00Z")),
    )

    @Test
    fun `engine contract is deterministic for the same snapshot`() {
        val engine = PlanningEngine { request ->
            PlanningResult(
                route = request.activities.map { RouteStep(it) },
                unscheduled = emptyList(),
            )
        }

        assertEquals(engine(input), engine(input))
    }

    @Test
    fun `planning input exposes a reusable snapshot without sharing activity list`() {
        val snapshot = input.snapshot()

        assertEquals(input, snapshot)
        assertNotSame(input.activities, snapshot.activities)
    }

    @Test
    fun `planning result explicitly represents a tradeoff`() {
        val result = PlanningResult(
            route = emptyList(),
            unscheduled = emptyList(),
            decisions = listOf(
                PlanningDecision(
                    activityId = "activity-1",
                    outcome = PlanningDecisionOutcome.DEFERRED,
                    reasons = listOf(PlanningReason.CAPACITY_EXCEEDED),
                ),
            ),
        )

        assertEquals(PlanningDecisionOutcome.DEFERRED, result.decisions.single().outcome)
        assertEquals(PlanningReason.CAPACITY_EXCEEDED, result.decisions.single().reasons.single())
    }
}
