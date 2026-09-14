package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstanceIds
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitDay
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
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

    @Test
    fun `materializes fixed event for the day`() {
        val eventDate = LocalDate.of(2026, 8, 10)
        val event = Event(
            id = EventId("event-1"),
            title = "Reunião",
            range = TimeRange(
                Instant.parse("2026-08-10T10:00:00Z"),
                Instant.parse("2026-08-10T11:00:00Z"),
            ),
            priority = Priority.REQUIRED,
        )

        val result = useCase(
            listOf(event),
            emptyList(),
            emptyList(),
            emptyList(),
            eventDate,
            ZoneOffset.UTC,
        )

        assertEquals("Reunião", result.single().title)
        assertEquals(Flexibility.FIXED, result.single().instance.flexibility)
        assertEquals(ActivityInstanceIds.forEvent(event.id, eventDate), result.single().instance.id)
        assertEquals(ActivitySource.FromEvent(event.id), result.single().instance.source)
    }

    @Test
    fun `materializes pending task with planned duration`() {
        val legacyDate = LocalDate.of(2026, 8, 17)
        val task = Task(
            id = TaskId("task-1"),
            title = "Relatório",
            plannedDuration = Duration.ofMinutes(45),
            priority = Priority.IMPORTANT,
            due = Instant.parse("2026-08-17T14:00:00Z"),
        )

        val result = useCase(
            emptyList(),
            listOf(task),
            emptyList(),
            emptyList(),
            legacyDate,
            ZoneOffset.UTC,
        )

        assertEquals("Relatório", result.single().title)
        assertEquals(Flexibility.FLEXIBLE, result.single().instance.flexibility)
        assertEquals(Duration.ofMinutes(45), result.single().instance.plannedDuration)
    }

    @Test
    fun `skips completed tasks and habits`() {
        val legacyDate = LocalDate.of(2026, 8, 17)
        val task = Task(
            id = TaskId("task-1"),
            title = "Feito",
            plannedDuration = Duration.ofMinutes(30),
            priority = Priority.IMPORTANT,
            completedAt = Instant.parse("2026-08-17T08:00:00Z"),
        )
        val habitDay = HabitDay(
            habit = Habit(
                id = HabitId("habit-1"),
                title = "Meditação",
                plannedDuration = Duration.ofMinutes(10),
                daysOfWeek = emptySet(),
            ),
            date = legacyDate,
            completedAt = Instant.parse("2026-08-17T07:00:00Z"),
        )

        val result = useCase(
            emptyList(),
            listOf(task),
            listOf(habitDay),
            emptyList(),
            legacyDate,
            ZoneOffset.UTC,
        )

        assertEquals(0, result.size)
    }

    @Test
    fun `materialized instances start pending`() {
        val legacyDate = LocalDate.of(2026, 8, 17)
        val task = Task(
            id = TaskId("task-1"),
            title = "Pendente",
            plannedDuration = Duration.ofMinutes(20),
            priority = Priority.DESIRABLE,
        )

        val result = useCase(
            emptyList(),
            listOf(task),
            emptyList(),
            emptyList(),
            legacyDate,
            ZoneOffset.UTC,
        )

        assertEquals(ActivityStatus.PENDING, result.single().instance.status)
    }
}
