package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyPersistedExecutionsTest {
    private val useCase = ApplyPersistedExecutions()
    private val plannedStart = Instant.parse("2026-01-01T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-01-01T10:00:00Z")

    @Test
    fun `overlays persisted status and actual time`() {
        val activity = pendingActivity()
        val actual = TimeRange(plannedStart, Instant.parse("2026-01-01T10:20:00Z"))
        val persisted = mapOf(
            activity.id to ActivityExecution(activity.id, ActivityStatus.DONE, activity.planned, actual),
        )

        val result = useCase(listOf(activity), persisted).single()

        assertEquals(ActivityStatus.DONE, result.status)
        assertEquals(actual, result.actual)
    }

    @Test
    fun `keeps materialized activity when no execution exists`() {
        val activity = pendingActivity()

        val result = useCase(listOf(activity), emptyMap()).single()

        assertEquals(ActivityStatus.PENDING, result.status)
        assertEquals(null, result.actual)
    }

    private fun pendingActivity() = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )
}
