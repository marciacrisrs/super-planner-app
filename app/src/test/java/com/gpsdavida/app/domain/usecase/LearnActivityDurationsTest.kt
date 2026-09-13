package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnActivityDurationsTest {
    @Test
    fun `learns median duration after minimum evidence`() {
        val base = Instant.parse("2026-09-13T09:00:00Z")
        val executions = listOf(30L, 45L, 60L).mapIndexed { index, minutes ->
            val day = base.plusSeconds(index * 86_400L)
            ActivityExecution(
                activityInstanceId = ActivityInstanceId("study"),
                status = ActivityStatus.DONE,
                planned = TimeRange(day, day.plusSeconds(1_800)),
                actualStart = day,
                actual = TimeRange(day, day.plusSeconds(minutes * 60)),
            )
        }

        val learned = LearnActivityDurations()(executions)

        assertEquals(45L, learned[ActivityInstanceId("study")]!!.toMinutes())
    }

    @Test
    fun `ignores small samples`() {
        val base = Instant.parse("2026-09-13T09:00:00Z")
        val executions = listOf(30L, 45L).mapIndexed { index, minutes ->
            val day = base.plusSeconds(index * 86_400L)
            ActivityExecution(
                activityInstanceId = ActivityInstanceId("study"),
                status = ActivityStatus.DONE,
                planned = TimeRange(day, day.plusSeconds(1_800)),
                actualStart = day,
                actual = TimeRange(day, day.plusSeconds(minutes * 60)),
            )
        }

        assertTrue(LearnActivityDurations()(executions).isEmpty())
    }
}
