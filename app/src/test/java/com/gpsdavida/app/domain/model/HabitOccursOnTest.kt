package com.superplanner.app.domain.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitOccursOnTest {

    @Test
    fun `empty days means every day`() {
        val habit = Habit(
            id = HabitId("h1"),
            title = "Água",
            plannedDuration = Duration.ofMinutes(5),
            daysOfWeek = emptySet(),
        )
        assertTrue(habit.occursOn(LocalDate.parse("2026-08-15")))
        assertTrue(habit.occursOn(LocalDate.parse("2026-08-16")))
    }

    @Test
    fun `weekday habit only on selected days`() {
        val habit = Habit(
            id = HabitId("h2"),
            title = "Correr",
            plannedDuration = Duration.ofMinutes(30),
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )
        assertTrue(habit.occursOn(LocalDate.parse("2026-08-17")))
        assertFalse(habit.occursOn(LocalDate.parse("2026-08-18")))
        assertTrue(habit.occursOn(LocalDate.parse("2026-08-19")))
    }
}
