package com.superplanner.app.domain.usecase

import com.superplanner.app.data.InMemoryRouteFeedbackRepository
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.RouteFeedbackReason
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteFeedbackUseCasesTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-13T20:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `feedback is stored separately from the activity history and remains queryable`() = runTest {
        val repository = InMemoryRouteFeedbackRepository()
        val record = RecordRouteFeedback(repository, clock)
        val activityId = ActivityInstanceId("activity-1")

        record(activityId, RouteFeedbackReason.DURATION_WRONG)
        record(activityId, RouteFeedbackReason.TIME_WRONG)

        val entries = repository.observeFor(activityId)
        var latest: List<com.superplanner.app.domain.model.RouteFeedback> = emptyList()
        entries.collect { latest = it }
        assertEquals(2, latest.size)
        assertEquals(RouteFeedbackReason.DURATION_WRONG, latest[0].reason)
        assertEquals(Instant.parse("2026-09-13T20:00:00Z"), latest[0].createdAt)
        assertTrue(latest.all { it.activityId == activityId })
    }
}