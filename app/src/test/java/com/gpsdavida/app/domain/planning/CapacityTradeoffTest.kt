package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapacityTradeoffTest {
    private val negotiator = CapacityTradeoffNegotiator()
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    @Test
    fun `detects overload and reports its size`() {
        val activities = listOf(
            activity("a", Duration.ofHours(2)),
            activity("b", Duration.ofHours(2)),
            activity("c", Duration.ofHours(2)),
        )

        val result = negotiator.analyze(activities, capacity(hours = 5))

        assertTrue(result.overloaded)
        assertEquals(Duration.ofHours(6), result.totalEstimated)
        assertEquals(Duration.ofHours(5), result.schedulableCapacity)
        assertEquals(Duration.ofHours(1), result.overload)
    }

    @Test
    fun `does not ask for negotiation when everything fits`() {
        val result = negotiator.analyze(
            listOf(activity("a", Duration.ofHours(1))),
            capacity(hours = 2),
        )

        assertFalse(result.overloaded)
        assertFalse(result.requiresUserDecision)
        assertTrue(result.alternatives.isEmpty())
    }

    @Test
    fun `fixed anchors are protected and alternatives contain explicit sacrifices`() {
        val activities = listOf(
            activity("anchor", Duration.ofHours(2), flexibility = Flexibility.FIXED, priority = Priority.LEISURE),
            activity("study", Duration.ofHours(2), priority = Priority.IMPORTANT),
            activity("house", Duration.ofHours(2), priority = Priority.DESIRABLE),
        )

        val result = negotiator.analyze(activities, capacity(hours = 4))

        assertEquals(listOf("activity-anchor"), result.protectedItems.map { it.id.value })
        assertTrue(result.requiresUserDecision)
        assertTrue(result.alternatives.isNotEmpty())
        assertTrue(result.alternatives.first().preservedItems.contains(ActivityInstanceId("activity-study")))
        assertTrue(result.alternatives.first().movedItems.contains(ActivityInstanceId("activity-house")))
    }

    @Test
    fun `user choice marks selected movable activities deferred`() {
        val activities = listOf(
            activity("study", Duration.ofHours(2), priority = Priority.IMPORTANT),
            activity("house", Duration.ofHours(2), priority = Priority.DESIRABLE),
        )
        val negotiation = negotiator.analyze(activities, capacity(hours = 2))
        val option = negotiation.alternatives.first()

        val result = negotiator.applyChoice(
            activities = activities,
            negotiation = negotiation,
            choice = TradeoffChoice(option.id),
        )

        assertEquals(ActivityStatus.PENDING, result.first { it.id == ActivityInstanceId("activity-study") }.status)
        assertEquals(ActivityStatus.DEFERRED, result.first { it.id == ActivityInstanceId("activity-house") }.status)
    }

    @Test
    fun `pending activities are considered while completed activities do not create overload`() {
        val completed = activity("done", Duration.ofHours(4)).completed(
            TimeRange(start, start.plus(Duration.ofHours(4))),
        )
        val pending = activity("pending", Duration.ofHours(2))

        val result = negotiator.analyze(listOf(completed, pending), capacity(hours = 2))

        assertFalse(result.overloaded)
        assertEquals(Duration.ofHours(2), result.totalEstimated)
    }

    private fun capacity(hours: Long) = DailyCapacity(
        normal = Duration.ofHours(hours),
        utilizationLimit = 1.0,
    )

    private fun activity(
        name: String,
        duration: Duration,
        flexibility: Flexibility = Flexibility.FLEXIBLE,
        priority: Priority = Priority.IMPORTANT,
    ) = ActivityInstance(
        id = ActivityInstanceId("activity-$name"),
        source = ActivitySource.FromTask(TaskId("task-$name")),
        flexibility = flexibility,
        planned = TimeRange(start, start.plus(duration)),
        priority = priority,
    )
}
