package com.superplanner.app.domain

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivityInstanceDurationTest {
    private val plannedStart = Instant.parse("2026-01-01T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-01-01T10:00:00Z")

    @Test
    fun `planned duration comes from planned time range`() {
        val activity = activity(plannedEnd)

        assertEquals(Duration.ofHours(1), activity.plannedDuration)
    }

    @Test
    fun `actual duration is null until activity is completed`() {
        val activity = activity(plannedEnd)

        assertNull(activity.actualDuration)
        assertNull(activity.durationVariance)
    }

    @Test
    fun `variance is positive when activity takes longer than planned`() {
        val activity = activity(plannedEnd)
            .completed(TimeRange(plannedStart, Instant.parse("2026-01-01T10:30:00Z")))

        assertEquals(Duration.ofMinutes(90), activity.actualDuration)
        assertEquals(Duration.ofMinutes(30), activity.durationVariance)
    }

    @Test
    fun `variance is negative when activity finishes early`() {
        val activity = activity(plannedEnd)
            .completed(TimeRange(plannedStart, Instant.parse("2026-01-01T09:45:00Z")))

        assertEquals(Duration.ofMinutes(-15), activity.durationVariance)
    }

    private fun activity(plannedEnd: Instant) = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )
}
