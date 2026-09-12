package com.superplanner.app.domain.model

import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventOccursOnTest {

    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(Instant.parse("2026-08-17T10:00:00Z"), zone)

    @Test
    fun `one-shot event only on its start date`() {
        val event = Event(
            id = EventId("e1"),
            title = "Dentista",
            range = TimeRange(
                Instant.parse("2026-08-17T13:00:00Z"),
                Instant.parse("2026-08-17T14:00:00Z"),
            ),
        )
        assertTrue(event.occursOn(LocalDate.now(clock), zone))
        assertFalse(event.occursOn(LocalDate.now(clock).plusDays(1), zone))
    }

    @Test
    fun `weekly event appears on matching days after start`() {
        val event = Event(
            id = EventId("e2"),
            title = "Aula",
            range = TimeRange(
                Instant.parse("2026-08-17T15:00:00Z"),
                Instant.parse("2026-08-17T16:00:00Z"),
            ),
            recurrenceDays = setOf(DayOfWeek.MONDAY),
        )
        assertTrue(event.occursOn(LocalDate.parse("2026-08-17"), zone))
        assertTrue(event.occursOn(LocalDate.parse("2026-08-24"), zone))
        assertFalse(event.occursOn(LocalDate.parse("2026-08-18"), zone))
        assertFalse(event.occursOn(LocalDate.parse("2026-08-10"), zone))
    }
}
