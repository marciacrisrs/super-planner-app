package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
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
            date = java.time.LocalDate.of(2026, 9, 13),
            zoneId = ZoneOffset.UTC,
            now = Instant.parse("2026-09-13T08:00:00Z"),
        )

        assertEquals(expected, result.activities)
    }

    private fun activity(id: String, start: String, end: String) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(
            Instant.parse("2026-09-13T$start:00Z"),
            Instant.parse("2026-09-13T$end:00Z"),
        ),
    )
}
