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
        assertEquals(RecalculationReason.USER_REQUESTED, engine.lastInput!!.recalculationReason)
        assertEquals(40 * 60, result.recalculation.result.route.single().activity.planned.start.epochSecond - start.epochSecond)
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
            return PlanningResult(route = input.activities.map { RouteStep(it) }, unscheduled = emptyList())
        }
    }
}
