package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterializeDailyActivitiesTest {
    private val useCase = MaterializeDailyActivities()
    private val zoneId = ZoneId.of("America/Sao_Paulo")
    private val date = LocalDate.of(2026, 9, 14)

    @Test
    fun fixed_start_is_materialized_at_exact_user_declared_time() {
        val fixedStart = date.atTime(18, 0).atZone(zoneId).toInstant()
        val task = Task(
            id = TaskId("task-fixed"),
            title = "Estudar francês",
            plannedDuration = Duration.ofHours(1),
            priority = Priority.IMPORTANT,
            due = fixedStart,
            fixedStartAt = fixedStart,
        )

        val activity = useCase(
            events = emptyList(),
            tasks = listOf(task),
            habits = emptyList(),
            routines = emptyList(),
            date = date,
            zoneId = zoneId,
        ).single().instance

        assertEquals(fixedStart, activity.planned.start)
        assertEquals(fixedStart.plus(Duration.ofHours(1)), activity.planned.end)
        assertEquals(Flexibility.FIXED, activity.flexibility)
    }

    @Test
    fun fixed_start_on_another_date_is_not_materialized_today() {
        val fixedStart = date.plusDays(1).atTime(18, 0).atZone(zoneId).toInstant()
        val task = Task(
            id = TaskId("task-future"),
            title = "Estudar francês",
            plannedDuration = Duration.ofHours(1),
            priority = Priority.IMPORTANT,
            due = fixedStart,
            fixedStartAt = fixedStart,
        )

        val activities = useCase(
            events = emptyList(),
            tasks = listOf(task),
            habits = emptyList(),
            routines = emptyList(),
            date = date,
            zoneId = zoneId,
        )

        assertTrue(activities.isEmpty())
    }

    @Test
    fun flexible_task_keeps_existing_due_anchor_behavior() {
        val due = date.atTime(LocalTime.of(16, 30)).atZone(zoneId).toInstant()
        val task = Task(
            id = TaskId("task-flexible"),
            title = "Responder e-mails",
            plannedDuration = Duration.ofMinutes(30),
            priority = Priority.IMPORTANT,
            due = due,
        )

        val activity = useCase(
            events = emptyList(),
            tasks = listOf(task),
            habits = emptyList(),
            routines = emptyList(),
            date = date,
            zoneId = zoneId,
        ).single().instance

        assertEquals(due, activity.planned.start)
        assertEquals(Flexibility.FLEXIBLE, activity.flexibility)
    }
}
