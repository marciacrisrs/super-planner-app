package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ReorganizeDayTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    @Test
    fun `delay is applied to a copy and route is recalculated by engine`() {
        val activity = activity("study")
        val input = PlanningInput(
            activities = listOf(activity),
            context = NextActionContext(now = start),
            previousRoute = RouteSnapshot(listOf(activity.id.value)),
        )
        val engine = RecordingEngine
        val useCase = ReorganizeDay(AdaptiveRouteRecalculator(engine))

        val result = useCase.execute(
            input,
            DayReorganizationRequest(
                operation = DayReorganizationOperation.DelayActivity(activity.id, 40),
                now = start,
            ),
        )

        assertEquals(start, input.activities.single().planned.start)
        assertEquals(start.plusSeconds(40 * 60), engine.lastInput!!.activities.single().planned.start)
        assertEquals(RecalculationReason.USER_REQUESTED, result.recalculation.reason)
        assertEquals(start.plusSeconds(40 * 60), result.recalculation.result.route.single().activity.planned.start)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `delay must be positive`() {
        ReorganizeDay(AdaptiveRouteRecalculator(RecordingEngine)).execute(
            PlanningInput(
                activities = listOf(activity("study")),
                context = NextActionContext(now = start),
            ),
            DayReorganizationRequest(
                operation = DayReorganizationOperation.DelayActivity(ActivityInstanceId("activity-study"), 0),
                now = start,
            ),
        )
    }

    private fun activity(name: String) = ActivityInstance(
        id = ActivityInstanceId("activity-$name"),
        source = ActivitySource.FromTask(TaskId("task-$name")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(start, start.plusSeconds(1800)),
    )

    private object RecordingEngine : PlanningEngine {
        var lastInput: PlanningInput? = null

        override fun invoke(input: PlanningInput): PlanningResult {
            lastInput = input
            return PlanningResult(
                route = input.activities.map { activity -> RouteStep(activity) },
                unscheduled = emptyList(),
            )
        }
    }
}
