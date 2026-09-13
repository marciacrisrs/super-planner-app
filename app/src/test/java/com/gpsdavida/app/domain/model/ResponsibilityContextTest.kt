package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsibilityContextTest {
    @Test
    fun `shared responsibility is optional and generic`() {
        val activity = ActivityInstance(
            id = ActivityInstanceId("care"),
            source = ActivitySource.FromTask(TaskId("care-task")),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(
                Instant.parse("2026-09-13T09:00:00Z"),
                Instant.parse("2026-09-13T09:30:00Z"),
            ),
            responsibility = ResponsibilityContext(
                scope = ResponsibilityScope.SHARED,
                participantLabel = "familiar",
            ),
        )

        assertEquals(ResponsibilityScope.SHARED, activity.responsibility.scope)
        assertEquals("familiar", activity.responsibility.participantLabel)
    }

    @Test
    fun `default activity remains personal`() {
        val activity = ActivityInstance(
            id = ActivityInstanceId("personal"),
            source = ActivitySource.FromTask(TaskId("personal-task")),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(
                Instant.parse("2026-09-13T09:00:00Z"),
                Instant.parse("2026-09-13T09:30:00Z"),
            ),
        )

        assertEquals(ResponsibilityScope.PERSONAL, activity.responsibility.scope)
    }
}
