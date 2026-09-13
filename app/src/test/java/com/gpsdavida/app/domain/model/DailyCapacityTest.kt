package com.superplanner.app.domain.model

import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyCapacityTest {
    @Test
    fun `normal capacity keeps a recovery margin`() {
        val capacity = DailyCapacity(normal = Duration.ofHours(8))

        assertEquals(Duration.ofMinutes(384), capacity.schedulable)
    }

    @Test
    fun `exceptional capacity overrides the normal baseline`() {
        val capacity = DailyCapacity(
            normal = Duration.ofHours(8),
            exceptional = Duration.ofHours(4),
            mode = CapacityMode.EXCEPTIONAL,
            utilizationLimit = 0.75,
        )

        assertEquals(Duration.ofMinutes(180), capacity.schedulable)
    }

    @Test
    fun `remaining capacity never becomes negative`() {
        val capacity = DailyCapacity(normal = Duration.ofHours(2))

        assertEquals(Duration.ZERO, capacity.remaining(Duration.ofHours(3)))
    }
}
