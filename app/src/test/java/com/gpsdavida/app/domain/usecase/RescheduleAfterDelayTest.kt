package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.TaskId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescheduleAfterDelayTest {
    private val date = "2026-08-16"
    private val start = Instant.parse("$date\u0000T09:00:00Z".replace("\u0000", ""))

    private val generator = GenerateDailySchedule()
    private val reschedule = RescheduleAfterDelay(generator)

    @Test
    fun `delayed execution pushes affected flexible activity but preserves fixed activity`() {
        val delayed = activity("delayed", "09:00:00", "10:00:00")
            .completed(TimeRange(instant("09:00:00"), instant("10:30:00")))
        val flexible = activity("flexible", "10:00:00", "11:00:00")
        val fixed = activity("fixed", "11:30:00", "12:30:00", Flexibility.FIXED)

        val result = reschedule(
            activities = listOf(delayed, flexible, fixed),
            delayedActivity = delayed,
        )

        val scheduledFlexible = result.activities.single { it.id == flexible.id }
        val scheduledFixed = result.activities.single { it.id == fixed.id }

        assertEquals(instant("10:30:00"), scheduledFlexible.planned.start)
        assertEquals(instant("11:30:00"), scheduledFlexible.planned.end)
        assertEquals(fixed.planned, scheduledFixed.planned)
        assertTrue(result.isConflictFree)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pending activity cannot trigger rescheduling`() {
        reschedule(
            activities = emptyList(),
            delayedActivity = activity("pending", "09:00:00", "10:00:00"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `on time completion cannot trigger rescheduling`() {
        val completed = activity("completed", "09:00:00", "10:00:00")
            .completed(TimeRange(instant("09:00:00"), instant("10:00:00")))

        reschedule(
            activities = listOf(completed),
            delayedActivity = completed,
        )
    }

    private fun activity(
        id: String,
        start: String,
        end: String,
        flexibility: Flexibility = Flexibility.FLEXIBLE,
    ) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId(id)),
        flexibility = flexibility,
        planned = TimeRange(instant(start), instant(end)),
    )

    private fun instant(time: String) = Instant.parse("2026-08-16T$time\u0000Z".replace("\u0000", ""))
}
