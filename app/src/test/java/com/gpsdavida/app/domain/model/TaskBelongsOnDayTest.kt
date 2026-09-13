package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskBelongsOnDayTest {

    private val zone = ZoneOffset.UTC

    @Test
    fun `open task with due today belongs on that day`() {
        val task = Task(
            id = TaskId("t1"),
            title = "Pagar conta",
            plannedDuration = Duration.ofMinutes(20),
            priority = Priority.REQUIRED,
            due = Instant.parse("2026-08-15T20:00:00Z"),
        )
        assertTrue(task.belongsOnDay(LocalDate.parse("2026-08-15"), zone))
        assertFalse(task.belongsOnDay(LocalDate.parse("2026-08-14"), zone))
    }

    @Test
    fun `overdue open task still belongs on later days`() {
        val task = Task(
            id = TaskId("t2"),
            title = "Atrasada",
            plannedDuration = Duration.ofMinutes(15),
            priority = Priority.IMPORTANT,
            due = Instant.parse("2026-08-10T12:00:00Z"),
        )
        assertTrue(task.belongsOnDay(LocalDate.parse("2026-08-15"), zone))
    }

    @Test
    fun `completed task belongs only on the completion date`() {
        val task = Task(
            id = TaskId("t3"),
            title = "Feita",
            plannedDuration = Duration.ofMinutes(10),
            priority = Priority.DESIRABLE,
            due = Instant.parse("2026-08-14T12:00:00Z"),
            completedAt = Instant.parse("2026-08-15T09:00:00Z"),
        )
        assertTrue(task.belongsOnDay(LocalDate.parse("2026-08-15"), zone))
        assertFalse(task.belongsOnDay(LocalDate.parse("2026-08-14"), zone))
    }
}
