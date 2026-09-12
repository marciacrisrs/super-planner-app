package com.superplanner.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class DurationEstimateTest {
    @Test
    fun taskDurationRemainsConfigurable() {
        val task = Task(
            id = TaskId("task-1"),
            title = "Estudar",
            plannedDuration = Duration.ofMinutes(45),
            priority = Priority.IMPORTANT,
        )

        assertEquals(Duration.ofMinutes(45), task.plannedDuration)
    }
}
