package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BuildPlanningInputTest {
    private val plannedStart = Instant.parse("2026-09-13T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-09-13T10:00:00Z")
    private val actualEnd = Instant.parse("2026-09-13T10:30:00Z")

    @Test
    fun `persisted delayed execution is included in planning input without viewmodel state`() {
        val activity = activity()
        val persisted = mapOf(
            activity.id to ActivityExecution(
                activityInstanceId = activity.id,
                status = ActivityStatus.DONE,
                planned = activity.planned,
                actualStart = plannedStart,
                actual = TimeRange(plannedStart, actualEnd),
            ),
        )
        val input = BuildPlanningInput(ApplyPersistedExecutions())(
            activities = listOf(activity),
            persisted = persisted,
            date = LocalDate.of(2026, 9, 13),
            context = NextActionContext(
                now = Instant.parse("2026-09-13T11:00:00Z"),
                zoneId = ZoneOffset.UTC,
            ),
        )

        assertEquals(ActivityStatus.DONE, input.activities.single().status)
        assertEquals(actualEnd, input.activities.single().actual?.end)
        assertSame(input.activities.single(), input.delayedActivity)
    }

    private fun activity() = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )
}
