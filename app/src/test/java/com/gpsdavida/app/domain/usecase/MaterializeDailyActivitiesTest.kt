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
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterializeDailyActivitiesTest {
    private val useCase = MaterializeDailyActivities()
    private val date = LocalDate.of(2026, 8, 17)
    private val zone = ZoneOffset.UTC

    @Test
    fun `materializes fixed event for the day`() {
        val event = Event(
            id = EventId("event-1"),
            title = "Reunião",
            range = TimeRange(
                Instant.parse("2026-08-10T10:00:00Z"),
                Instant.parse("2026-08-10T11:00:00Z"),
            ),
            priority = Priority.REQUIRED,
        )

        val result = useCase(listOf(event), emptyList(), emptyList(), emptyList(), date, zone)

        assertEquals("Reunião", result.single().title)
        assertEquals(Flexibility.FIXED, result.single().instance.flexibility)
        assertEquals(
            ActivityInstanceIds.forEvent(event.id, date),
            result.single().instance.id,
        )
        assertEquals(ActivitySource.FromEvent(event.id), result.single().instance.source)
    }

    @Test
    fun `materializes pending task with planned duration`() {
        val task = Task(
            id = TaskId("task-1"),
            title = "Relatório",
            plannedDuration = Duration.ofMinutes(45),
            priority = Priority.IMPORTANT,
            due = Instant.parse("2026-08-17T14:00:00Z"),
        )

        val result = useCase(emptyList(), listOf(task), emptyList(), emptyList(), date, zone)

        assertEquals("Relatório", result.single().title)
        assertEquals(Flexibility.FLEXIBLE, result.single().instance.flexibility)
        assertEquals(Duration.ofMinutes(45), result.single().instance.plannedDuration)
    }

    @Test
    fun `skips completed tasks and habits`() {
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
            date = date,
            completedAt = Instant.parse("2026-08-17T07:00:00Z"),
        )

        val result = useCase(emptyList(), listOf(task), listOf(habitDay), emptyList(), date, zone)

        assertEquals(0, result.size)
    }

    @Test
    fun `materialized instances start pending`() {
        val task = Task(
            id = TaskId("task-1"),
            title = "Pendente",
            plannedDuration = Duration.ofMinutes(20),
            priority = Priority.DESIRABLE,
        )

        val result = useCase(emptyList(), listOf(task), emptyList(), emptyList(), date, zone)

        assertEquals(ActivityStatus.PENDING, result.single().instance.status)
    }
}
