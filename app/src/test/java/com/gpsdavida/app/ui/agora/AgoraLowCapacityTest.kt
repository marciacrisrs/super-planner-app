package com.superplanner.app.ui.agora

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgoraLowCapacityTest {
    @Test
    fun `maps low-capacity mode and its summary to UI state`() {
        val activity = DailyActivity(
            title = "Estudo",
            instance = ActivityInstance(
                id = ActivityInstanceId("study"),
                source = ActivitySource.FromTask(TaskId("study")),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(
                    Instant.parse("2026-09-13T10:00:00Z"),
                    Instant.parse("2026-09-13T11:00:00Z"),
                ),
                priority = Priority.IMPORTANT,
            ),
        )

        val state = AgoraUiMapper.map(
            activities = listOf(activity),
            decision = NextActionDecision(current = activity.instance, next = null),
            now = Instant.parse("2026-09-13T10:00:00Z"),
            zoneId = ZoneOffset.UTC,
            lowCapacity = true,
            lowCapacitySummary = LowCapacitySummary(preserved = 2, moved = 1, deferred = 3),
        )

        assertTrue(state.lowCapacity)
        assertEquals(2, state.lowCapacitySummary.preserved)
        assertEquals(1, state.lowCapacitySummary.moved)
        assertEquals(3, state.lowCapacitySummary.deferred)
    }
}
