package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.CapacityMode
import com.superplanner.app.domain.model.DailyCapacity
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LowCapacityPlannerTest {
    @Test
    fun `low capacity halves declared capacity without changing normal baseline`() {
        val normal = DailyCapacity(normal = Duration.ofHours(8), utilizationLimit = 0.8)

        val low = LowCapacityPlanner.capacity(normal)

        assertEquals(Duration.ofHours(8), low.normal)
        assertEquals(Duration.ofHours(4), low.declared)
        assertEquals(Duration.ofHours(3).plus(Duration.ofMinutes(12)), low.schedulable)
        assertEquals(CapacityMode.EXCEPTIONAL, low.mode)
    }

    @Test
    fun `default low capacity works without configuring the user`() {
        val low = LowCapacityPlanner.capacity()

        assertEquals(Duration.ofHours(4), low.declared)
        assertTrue(low.schedulable > Duration.ZERO)
    }
}
