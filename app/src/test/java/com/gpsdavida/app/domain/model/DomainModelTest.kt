package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainModelTest {

    @Test
    fun `event is fixed and task is flexible`() {
        val event = Event(
            id = EventId("e1"),
            title = "Reunião",
            range = TimeRange(Instant.parse("2026-08-15T12:00:00Z"), Instant.parse("2026-08-15T13:00:00Z")),
        )
        val task = Task(
            id = TaskId("t1"),
            title = "Escrever ADR",
            plannedDuration = Duration.ofMinutes(45),
            priority = Priority.IMPORTANT,
        )
        assertEquals(Flexibility.FIXED, event.flexibility)
        assertEquals(Flexibility.FLEXIBLE, task.flexibility)
    }

    @Test
    fun `completing an instance records actual duration separately from planned`() {
        val planned = TimeRange(
            Instant.parse("2026-08-15T14:00:00Z"),
            Instant.parse("2026-08-15T15:00:00Z"),
        )
        val pending = ActivityInstance(
            id = ActivityInstanceId("i1"),
            source = ActivitySource.FromTask(TaskId("t1")),
            flexibility = Flexibility.FLEXIBLE,
            planned = planned,
        )
        val actual = TimeRange(
            Instant.parse("2026-08-15T14:10:00Z"),
            Instant.parse("2026-08-15T15:25:00Z"),
        )
        val done = pending.completed(actual)
        assertNull(pending.actualDuration)
        assertEquals(Duration.ofHours(1), done.plannedDuration)
        assertEquals(Duration.ofMinutes(75), done.actualDuration)
        assertEquals(ActivityStatus.DONE, done.status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `time range rejects inverted bounds`() {
        TimeRange(Instant.parse("2026-08-15T15:00:00Z"), Instant.parse("2026-08-15T14:00:00Z"))
    }
}
