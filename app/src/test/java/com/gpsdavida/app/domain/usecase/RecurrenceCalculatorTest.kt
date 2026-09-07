package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.domain.model.RecurrenceRule
import com.gpsdavida.app.domain.model.RecurrenceUnit
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurrenceCalculatorTest {
    private val calculator = RecurrenceCalculator()

    @Test
    fun everyTwoMonths_matchesOnlyTheConfiguredDay() {
        val rule = RecurrenceRule(
            startDate = LocalDate.of(2026, 1, 15),
            interval = 2,
            unit = RecurrenceUnit.MONTH,
        )

        assertTrue(calculator.occursOn(rule, LocalDate.of(2026, 3, 15)))
        assertFalse(calculator.occursOn(rule, LocalDate.of(2026, 4, 15)))
        assertFalse(calculator.occursOn(rule, LocalDate.of(2026, 3, 16)))
    }

    @Test
    fun weeklyRule_canLimitDaysOfWeek() {
        val rule = RecurrenceRule(
            startDate = LocalDate.of(2026, 9, 7),
            interval = 1,
            unit = RecurrenceUnit.WEEK,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )

        assertTrue(calculator.occursOn(rule, LocalDate.of(2026, 9, 7)))
        assertTrue(calculator.occursOn(rule, LocalDate.of(2026, 9, 9)))
        assertFalse(calculator.occursOn(rule, LocalDate.of(2026, 9, 8)))
    }

    @Test
    fun nextOccurrence_respectsEndDate() {
        val rule = RecurrenceRule(
            startDate = LocalDate.of(2026, 9, 7),
            endDate = LocalDate.of(2026, 9, 9),
            unit = RecurrenceUnit.DAY,
        )

        assertEquals(LocalDate.of(2026, 9, 8), calculator.nextOccurrence(rule, LocalDate.of(2026, 9, 7)))
        assertEquals(LocalDate.of(2026, 9, 9), calculator.nextOccurrence(rule, LocalDate.of(2026, 9, 8)))
        assertEquals(null, calculator.nextOccurrence(rule, LocalDate.of(2026, 9, 9)))
    }
}
