package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanningEngineIntegrationTest {
    @Test
    fun `compatibility recalculation exposes the engine route`() {
        val first = activity("first", "09:00", "10:00")
        val second = activity("second", "10:00", "11:00")
        val expected = listOf(second, first)
        val engine = PlanningEngine {
            PlanningResult(route = expected.map { RouteStep(it) }, unscheduled = emptyList())
        }

        val result = com.superplanner.app.domain.usecase.RecalculateRoute(engine)(
            activities = listOf(first, second),
            date = LocalDate.of(2026, 9, 13),
            zoneId = ZoneOffset.UTC,
            now = Instant.parse("2026-09-13T08:00:00Z"),
        )

        assertEquals(expected, result.activities)
    }

    @Test
    fun `compatibility recalculation preserves the requested date independently from now`() {
        val requestedDate = LocalDate.of(2026, 9, 14)
        val activityOnPreviousDay = activity("previous-day", "09:00", "10:00")
        var captured: PlanningInput? = null
        val engine = PlanningEngine { input ->
            captured = input
            PlanningResult(route = emptyList(), unscheduled = emptyList())
        }

        com.superplanner.app.domain.usecase.RecalculateRoute(engine)(
            activities = listOf(activityOnPreviousDay),
            date = requestedDate,
            zoneId = ZoneOffset.UTC,
            now = Instant.parse("2026-09-13T08:00:00Z"),
        )

        assertEquals(requestedDate, captured!!.date)
    }

    private fun activity(id: String, start: String, end: String) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(com.superplanner.app.domain.model.TaskId("task-$id")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(
            Instant.parse("2026-09-13T$start:00Z"),
            Instant.parse("2026-09-13T$end:00Z"),
        ),
    )
}
