package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveRouteRecalculatorTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    @Test
    fun `recalculation records the reason and does not mutate the input`() {
        val first = activity("first")
        val second = activity("second")
        val input = PlanningInput(
            activities = listOf(first, second),
            context = context(),
            previousRoute = RouteSnapshot(listOf(first.id.value, second.id.value)),
        )
        val engine = RecordingEngine { listOf(second, first) }
        val recalculator = AdaptiveRouteRecalculator(engine)

        val result = recalculator.recalculate(input, RecalculationReason.EXECUTION_CHANGED)

        assertEquals(RecalculationReason.EXECUTION_CHANGED, result.reason)
        assertEquals(
            listOf(first.id.value, second.id.value),
            input.activities.map { it.id.value },
        )
        assertEquals(
            listOf(second.id.value, first.id.value),
            result.currentRoute.orderedActivityIds,
        )
        assertTrue(result.routeChanged)
        assertEquals(RecalculationReason.EXECUTION_CHANGED, engine.lastInput?.recalculationReason)
    }

    @Test
    fun `new item is reported as added`() {
        val first = activity("first")
        val second = activity("second")
        val input = PlanningInput(
            activities = listOf(first, second),
            context = context(),
            previousRoute = RouteSnapshot(listOf(first.id.value)),
        )
        val recalculator = AdaptiveRouteRecalculator(RecordingEngine { listOf(first, second) })

        val result = recalculator.recalculate(input, RecalculationReason.ACTIVITY_CHANGED)

        assertEquals(
            RouteChange(
                activityId = second.id,
                kind = RouteChangeKind.ADDED,
                previousPosition = null,
                currentPosition = 1,
            ),
            result.changes.single(),
        )
    }

    @Test
    fun `removed item is reported without changing execution history`() {
        val first = activity("first")
        val input = PlanningInput(
            activities = listOf(first),
            context = context(),
            previousRoute = RouteSnapshot(listOf(first.id.value, "activity-removed")),
        )
        val recalculator = AdaptiveRouteRecalculator(RecordingEngine { listOf(first) })

        val result = recalculator.recalculate(input, RecalculationReason.ACTIVITY_CHANGED)

        assertEquals(RouteChangeKind.REMOVED, result.changes.single().kind)
        assertEquals(ActivityInstanceId("activity-removed"), result.changes.single().activityId)
        assertEquals(1, result.changes.single().previousPosition)
        assertEquals(null, result.changes.single().currentPosition)
    }

    @Test
    fun `move diff is deterministic by activity id`() {
        val a = activity("a")
        val b = activity("b")
        val c = activity("c")
        val input = PlanningInput(
            activities = listOf(a, b, c),
            context = context(),
            previousRoute = RouteSnapshot(listOf(a.id.value, b.id.value, c.id.value)),
        )
        val recalculator = AdaptiveRouteRecalculator(RecordingEngine { listOf(c, b, a) })

        val first = recalculator.recalculate(input, RecalculationReason.TIME_ADVANCED)
        val second = recalculator.recalculate(input, RecalculationReason.TIME_ADVANCED)

        assertEquals(first.changes, second.changes)
        assertEquals(
            listOf("activity-a", "activity-c"),
            first.changes.map { it.activityId.value },
        )
        assertFalse(first.hasUnscheduledActivities)
    }

    private fun context() = com.superplanner.app.domain.model.NextActionContext(now = start)

    private fun activity(name: String) = ActivityInstance(
        id = ActivityInstanceId("activity-$name"),
        source = ActivitySource.FromTask(TaskId("task-$name")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(start, start.plusSeconds(1800)),
    )

    private class RecordingEngine(
        private val route: () -> List<ActivityInstance>,
    ) : PlanningEngine {
        var lastInput: PlanningInput? = null
            private set

        override fun invoke(input: PlanningInput): PlanningResult {
            lastInput = input
            return PlanningResult(
                route = route().map { activity -> RouteStep(activity) },
                unscheduled = emptyList(),
            )
        }
    }
}
