package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChooseNextActivityCapacityTest {
    private val useCase = ChooseNextActivity()
    private val now = Instant.parse("2026-08-17T10:00:00Z")
    private val zone = ZoneOffset.UTC

    @Test
    fun `next action respects realistic daily capacity instead of filling every free minute`() {
        val first = activity("first", "11:00:00", "11:30:00", Priority.IMPORTANT)
        val second = activity("second", "11:30:00", "12:30:00", Priority.REQUIRED)

        val decision = useCase(
            listOf(first, second),
            NextActionContext(
                now = now,
                zoneId = zone,
                dailyCapacity = DailyCapacity(normal = Duration.ofMinutes(60)),
            ),
        )

        assertEquals(first.id, decision.next?.id)
        assertEquals(true, decision.nextReasons.contains(NextActionReason.CAPACITY_AVAILABLE))
    }

    @Test
    fun `activity that exceeds realistic capacity is not recommended`() {
        val longTask = activity("long", "11:00:00", "12:00:00", Priority.REQUIRED)

        val decision = useCase(
            listOf(longTask),
            NextActionContext(
                now = now,
                zoneId = zone,
                dailyCapacity = DailyCapacity(normal = Duration.ofMinutes(60)),
            ),
        )

        assertNull(decision.next)
    }

    @Test
    fun `observed duration is used for completed work without changing future estimates`() {
        val completed = activity("completed", "08:00:00", "08:30:00", Priority.IMPORTANT).copy(
            status = ActivityStatus.DONE,
            actual = TimeRange(
                Instant.parse("2026-08-17T08:00:00Z"),
                Instant.parse("2026-08-17T09:00:00Z"),
            ),
        )
        val next = activity("next", "11:00:00", "11:30:00", Priority.IMPORTANT)

        val decision = useCase(
            listOf(completed, next),
            NextActionContext(
                now = now,
                zoneId = zone,
                dailyCapacity = DailyCapacity(normal = Duration.ofMinutes(90)),
            ),
        )

        assertNull(decision.next)
        assertEquals(Duration.ofMinutes(30), next.plannedDuration)
    }

    private fun activity(
        id: String,
        start: String,
        end: String,
        priority: Priority,
    ) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId(id)),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(
            Instant.parse("2026-08-17T${start}Z"),
            Instant.parse("2026-08-17T${end}Z"),
        ),
        priority = priority,
    )
}
