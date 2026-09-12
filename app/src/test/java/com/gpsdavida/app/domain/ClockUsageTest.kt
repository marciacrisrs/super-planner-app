package com.superplanner.app.domain

import java.time.Clock
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockUsageTest {

    @Test
    fun `fixed clock returns the injected instant`() {
        val instant = Instant.parse("2026-08-15T18:00:00Z")
        val clock = Clock.fixed(instant, java.time.ZoneOffset.UTC)
        assertEquals(instant, clock.instant())
    }
}
