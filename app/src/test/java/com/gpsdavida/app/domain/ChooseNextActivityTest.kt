package com.superplanner.app.domain

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.usecase.ChooseNextActivity
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChooseNextActivityTest {
    private val choose = ChooseNextActivity()
    private val now = Instant.parse("2026-08-16T12:00:00Z")

    @Test
    fun `current activity wins over overdue activity`() {
        val current = activity("current", "11:30", "12:30", Priority.IMPORTANT)
        val overdue = activity("overdue", "10:00", "11:00", Priority.REQUIRED)

        assertEquals("current", choose(listOf(overdue, current), now)?.id?.value)
    }

    @Test
    fun `overdue activity wins over future activity`() {
        val overdue = activity("overdue", "10:00", "11:00", Priority.DESIRABLE)
        val future = activity("future", "13:00", "14:00", Priority.REQUIRED)

        assertEquals("overdue", choose(listOf(future, overdue), now)?.id?.value)
    }

    @Test
    fun `higher priority wins among overdue activities`() {
        val important = activity("important", "09:00", "10:00", Priority.IMPORTANT)
        val required = activity("required", "10:00", "11:00", Priority.REQUIRED)

        assertEquals("required", choose(listOf(important, required), now)?.id?.value)
    }

    @Test
    fun `fixed activity wins when urgency and priority tie`() {
        val flexible = activity("flexible", "10:00", "11:00", Priority.IMPORTANT, Flexibility.FLEXIBLE)
        val fixed = activity("fixed", "10:30", "11:30", Priority.IMPORTANT, Flexibility.FIXED)

        assertEquals("fixed", choose(listOf(flexible, fixed), now)?.id?.value)
    }

    @Test
    fun `completed skipped and deferred activities are ignored`() {
        val done = activity("done", "09:00", "10:00", Priority.REQUIRED).copy(status = ActivityStatus.DONE)
        val skipped = activity("skipped", "10:00", "11:00", Priority.REQUIRED).copy(status = ActivityStatus.SKIPPED)
        val deferred = activity("deferred", "11:00", "11:30", Priority.REQUIRED).copy(status = ActivityStatus.DEFERRED)

        assertNull(choose(listOf(done, skipped, deferred), now))
    }

    @Test
    fun `chronological order breaks equal urgency priority and flexibility`() {
        val later = activity("later", "10:30", "11:00", Priority.IMPORTANT, Flexibility.FIXED)
        val earlier = activity("earlier", "09:30", "10:00", Priority.IMPORTANT, Flexibility.FIXED)

        assertEquals("earlier", choose(listOf(later, earlier), now)?.id?.value)
    }

    private fun activity(
        id: String,
        start: String,
        end: String,
        priority: Priority,
        flexibility: Flexibility = Flexibility.FIXED,
    ) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = flexibility,
        planned = TimeRange(
            Instant.parse("2026-08-16T${start}:00Z"),
            Instant.parse("2026-08-16T${end}:00Z"),
        ),
        priority = priority,
    )
}
