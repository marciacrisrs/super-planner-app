package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryPlannerTest {
    private val recoveryStart = LocalDate.of(2026, 9, 14)
    private val planner = RecoveryPlanner()
    private val capacity = DailyCapacity(normal = Duration.ofHours(4), utilizationLimit = 1.0)

    @Test
    fun `redistributes only relevant missed work inside the recovery horizon`() {
        val important = activity("important", "2026-09-13T09:00:00Z", "2026-09-13T10:00:00Z", Priority.IMPORTANT)
        val leisure = activity("leisure", "2026-09-13T11:00:00Z", "2026-09-13T12:00:00Z", Priority.LEISURE)

        val plan = planner(
            activities = listOf(important, leisure),
            recoveryStart = recoveryStart,
            capacities = capacitiesFor(7),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals("important", plan.scheduled.single().item.activity.id.value)
        assertEquals(recoveryStart, plan.scheduled.single().date)
        assertEquals(1, plan.reassess.size)
        assertEquals("leisure", plan.reassess.single().activity.id.value)
    }

    @Test
    fun `preserves future commitments when recalculating recovery capacity`() {
        val missed = activity("missed", "2026-09-13T09:00:00Z", "2026-09-13T11:00:00Z", Priority.IMPORTANT)
        val tomorrow = activity("tomorrow", "2026-09-14T09:00:00Z", "2026-09-14T12:00:00Z", Priority.DESIRABLE)

        val plan = planner(
            activities = listOf(missed, tomorrow),
            recoveryStart = recoveryStart,
            capacities = capacitiesFor(7),
            zoneId = ZoneOffset.UTC,
        )

        assertTrue(plan.reassess.any { it.activity.id.value == "missed" })
        assertEquals(Duration.ofHours(1), plan.capacityByDay.getValue(recoveryStart).remaining)
    }

    @Test
    fun `deadline gets precedence even after a bad day`() {
        val overdue = activity(
            id = "deadline",
            start = "2026-09-13T09:00:00Z",
            end = "2026-09-13T10:00:00Z",
            priority = Priority.DESIRABLE,
            dueAt = Instant.parse("2026-09-14T23:59:00Z"),
        )

        val plan = planner(
            activities = listOf(overdue),
            recoveryStart = recoveryStart,
            capacities = capacitiesFor(7),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(RecoveryReason.DEADLINE, plan.assignments.single().reason)
        assertEquals(RecoveryAction.REDISTRIBUTE, plan.assignments.single().action)
    }

    private fun capacitiesFor(days: Int): Map<LocalDate, DailyCapacity> =
        (0 until days).associate { recoveryStart.plusDays(it.toLong()) to capacity }

    private fun activity(
        id: String,
        start: String,
        end: String,
        priority: Priority,
        dueAt: Instant? = null,
    ) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = com.superplanner.app.domain.model.Flexibility.FLEXIBLE,
        planned = TimeRange(Instant.parse(start), Instant.parse(end)),
        priority = priority,
        dueAt = dueAt,
    )
}
