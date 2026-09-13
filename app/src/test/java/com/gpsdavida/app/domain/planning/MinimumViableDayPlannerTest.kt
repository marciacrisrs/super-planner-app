package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.MinimumViableDay
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class MinimumViableDayPlannerTest {
    @Test
    fun `protected essentials stay ahead of optional work`() {
        val start = Instant.parse("2026-09-13T09:00:00Z")
        val essential = activity("essential", Priority.IMPORTANT, start.plusSeconds(7_200))
        val optional = activity("optional", Priority.REQUIRED, start)

        val ordered = MinimumViableDayPlanner.order(
            listOf(optional, essential),
            MinimumViableDay(setOf(essential.id)),
        )

        assertEquals(essential.id, ordered.first().id)
        assertEquals(optional.id, ordered.last().id)
    }

    private fun activity(id: String, priority: Priority, start: Instant) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(start, start.plusSeconds(1800)),
        priority = priority,
    )
}
