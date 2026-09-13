package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertTrue
import org.junit.Test

class ChooseNextActivityExplainabilityTest {
    private val useCase = ChooseNextActivity()
    private val now = Instant.parse("2026-08-17T10:00:00Z")

    @Test
    fun `recommended action exposes higher priority as a reason`() {
        val important = activity("important", "09:30", "10:30", Priority.IMPORTANT)
        val required = activity("required", "09:45", "10:15", Priority.REQUIRED)

        val decision = useCase(
            listOf(important, required),
            NextActionContext(now = now, zoneId = ZoneOffset.UTC),
        )

        assertTrue(decision.recommendedReasons.contains(NextActionReason.HIGHER_PRIORITY))
    }

    private fun activity(id: String, start: String, end: String, priority: Priority) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId(id)),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(
            Instant.parse("2026-08-17T$start:00Z"),
            Instant.parse("2026-08-17T$end:00Z"),
        ),
        priority = priority,
    )
}
