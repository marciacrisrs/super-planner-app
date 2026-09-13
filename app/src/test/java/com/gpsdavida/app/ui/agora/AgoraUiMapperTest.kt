package com.superplanner.app.ui.agora

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.ui.next.NextActionState
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgoraUiMapperTest {
    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-08-17T10:00:00Z")

    @Test
    fun `maps recommended activity as main focus`() {
        val current = daily("now", "10:00", "10:30")
        val decision = NextActionDecision(current = current.instance, next = null)

        val state = AgoraUiMapper.map(listOf(current), decision, now, zone)

        assertEquals("now", state.title)
        assertEquals(LocalTime.of(10, 0), state.currentTime)
        assertEquals(NextActionState.Ready, state.state)
    }

    @Test
    fun `next and later queues exclude the main activity`() {
        val current = daily("now", "10:00", "10:30")
        val next = daily("next", "10:30", "11:00")
        val later = daily("later", "11:00", "11:30")
        val decision = NextActionDecision(current = current.instance, next = next.instance)

        val state = AgoraUiMapper.map(listOf(current, next, later), decision, now, zone)

        assertEquals("next", state.nextUpcoming?.title)
        assertEquals(listOf("later"), state.laterUpcoming.map { it.title })
    }

    @Test
    fun `all done shows completed state`() {
        val done = daily("done", "09:00", "09:30").copy(
            instance = daily("done", "09:00", "09:30").instance.copy(status = ActivityStatus.DONE),
        )
        val decision = NextActionDecision(current = null, next = null)

        val state = AgoraUiMapper.map(listOf(done), decision, now, zone)

        assertEquals(NextActionState.Completed, state.state)
        assertNull(state.currentActivity)
    }

    private fun daily(title: String, start: String, end: String): DailyActivity {
        val startInstant = Instant.parse("2026-08-17T${start}:00Z")
        val endInstant = Instant.parse("2026-08-17T${end}:00Z")
        return DailyActivity(
            title = title,
            instance = ActivityInstance(
                id = ActivityInstanceId(title),
                source = ActivitySource.FromTask(TaskId(title)),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(startInstant, endInstant),
                priority = Priority.IMPORTANT,
            ),
        )
    }
}
