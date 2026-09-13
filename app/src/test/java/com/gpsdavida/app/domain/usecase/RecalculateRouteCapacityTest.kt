package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.planning.PlanningDecision
import com.superplanner.app.domain.planning.PlanningDecisionOutcome
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.planning.PlanningInput
import com.superplanner.app.domain.planning.PlanningReason
import com.superplanner.app.domain.planning.PlanningResult
import com.superplanner.app.domain.planning.RouteStep
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RecalculateRouteCapacityTest {
    @Test
    fun `passes daily capacity to planning engine`() {
        var captured: PlanningInput? = null
        val engine = PlanningEngine { input ->
            captured = input
            PlanningResult(
                route = input.activities.map { RouteStep(it) },
                unscheduled = emptyList(),
                decisions = input.activities.map {
                    PlanningDecision(it.id.value, PlanningDecisionOutcome.SCHEDULED, listOf(PlanningReason.PRIORITY_PRESERVED))
                },
            )
        }
        val capacity = DailyCapacity(normal = Duration.ofHours(8), exceptional = Duration.ofHours(4), mode = com.superplanner.app.domain.model.CapacityMode.EXCEPTIONAL)
        val activity = ActivityInstance(
            id = ActivityInstanceId("a"),
            source = com.superplanner.app.domain.model.ActivitySource.FromTask(com.superplanner.app.domain.model.TaskId("t")),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(Instant.parse("2026-09-13T10:00:00Z"), Instant.parse("2026-09-13T11:00:00Z")),
            priority = Priority.IMPORTANT,
        )

        RecalculateRoute(engine)(
            activities = listOf(activity),
            date = java.time.LocalDate.of(2026, 9, 13),
            zoneId = ZoneOffset.UTC,
            now = Instant.parse("2026-09-13T09:00:00Z"),
            dailyCapacity = capacity,
        )

        assertSame(capacity, captured?.context?.dailyCapacity)
        assertEquals(java.time.LocalDate.of(2026, 9, 13), captured?.date)
    }
}
