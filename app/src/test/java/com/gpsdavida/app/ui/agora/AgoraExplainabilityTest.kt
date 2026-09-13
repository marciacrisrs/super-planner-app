package com.superplanner.app.ui.agora

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.ui.next.NextActionState
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgoraExplainabilityTest {
    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-08-17T10:00:00Z")

    @Test
    fun `maps decision reasons to a human explanation`() {
        val current = daily("current", "09:30", "10:30", Priority.REQUIRED)
        val decision = NextActionDecision(
            current = current.instance,
            next = null,
            currentReasons = listOf(NextActionReason.HIGHER_PRIORITY, NextActionReason.DUE_NOW),
        )

        val state = AgoraUiMapper.map(listOf(current), decision, now, zone)

        assertEquals(
            "Escolhi agora porque tem prioridade mais alta e já pode ser feita.",
            state.explanation,
        )
    }

    @Test
    fun `explains why the next activity is left for later`() {
        val current = daily("current", "09:30", "10:30", Priority.REQUIRED)
        val next = daily("next", "10:30", "11:00", Priority.IMPORTANT)
        val decision = NextActionDecision(current = current.instance, next = next.instance)

        val state = AgoraUiMapper.map(listOf(current, next), decision, now, zone)

        assertTrue(state.nextUpcoming?.explanation?.contains("prioridade maior") == true)
        assertEquals(NextActionState.Ready, state.state)
    }

    private fun daily(title: String, start: String, end: String, priority: Priority): DailyActivity {
        val startInstant = Instant.parse("2026-08-17T$start:00Z")
        val endInstant = Instant.parse("2026-08-17T$end:00Z")
        return DailyActivity(
            title = title,
            instance = ActivityInstance(
                id = ActivityInstanceId(title),
                source = ActivitySource.FromTask(TaskId(title)),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(startInstant, endInstant),
                priority = priority,
            ),
        )
    }
}
