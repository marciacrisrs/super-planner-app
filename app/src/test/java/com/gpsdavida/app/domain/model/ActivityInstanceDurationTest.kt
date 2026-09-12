package com.superplanner.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ActivityInstanceDurationTest {
    private val start = Instant.parse("2026-08-16T09:00:00Z")

    @Test
    fun plannedDurationComesFromPlannedRange() {
        assertEquals(Duration.ofMinutes(30), activity(30).plannedDuration)
    }

    @Test
    fun actualDurationIsAvailableAfterCompletion() {
        val completed = activity(30).completed(range(42))

        assertEquals(Duration.ofMinutes(42), completed.actualDuration)
        assertEquals(ActivityStatus.DONE, completed.status)
    }

    @Test
    fun durationVarianceIsActualMinusPlanned() {
        assertEquals(Duration.ofMinutes(10), activity(30, 40).durationVariance)
        assertEquals(Duration.ofMinutes(-5), activity(30, 25).durationVariance)
    }

    @Test
    fun durationVarianceIsNullBeforeExecution() {
        assertEquals(null, activity(30).durationVariance)
    }

    private fun activity(plannedMinutes: Long, actualMinutes: Long? = null): ActivityInstance =
        ActivityInstance(
            id = ActivityInstanceId("activity-1"),
            source = ActivitySource.FromTask(TaskId("task-1")),
            flexibility = Flexibility.FLEXIBLE,
            planned = range(plannedMinutes),
            actual = actualMinutes?.let(::range),
        )

    private fun range(minutes: Long): TimeRange =
        TimeRange(start, start.plusSeconds(minutes * 60))
}
