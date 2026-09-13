package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.RecurrenceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class NaturalLanguageActivityParserTest {
    private val today = LocalDate.of(2026, 9, 13)

    @Test
    fun `extracts date duration and time`() {
        val draft = NaturalLanguageActivityParser.parse(
            "amanhã estudar francês por uma hora às 19:30",
            today,
        )

        assertEquals("francês", draft.title)
        assertEquals(Duration.ofHours(1), draft.plannedDuration)
        assertEquals(today.plusDays(1), draft.date)
        assertEquals(LocalTime.of(19, 30), draft.startTime)
        assertTrue(draft.missingFields.isEmpty())
    }

    @Test
    fun `extracts daily recurrence without inventing duration`() {
        val draft = NaturalLanguageActivityParser.parse("estudar inglês todo dia", today)

        assertEquals("inglês", draft.title)
        assertEquals(RecurrenceUnit.DAY, draft.recurrence?.unit)
        assertTrue(draft.plannedDuration == null)
        assertTrue(draft.missingFields.contains(MissingActivityField.DURATION))
    }

    @Test
    fun `requires confirmation data to remain explicit when information is missing`() {
        val draft = NaturalLanguageActivityParser.parse("amanhã preciso estudar", today)

        assertEquals("", draft.title)
        assertTrue(draft.missingFields.contains(MissingActivityField.TITLE))
        assertTrue(draft.missingFields.contains(MissingActivityField.DURATION))
    }
}
