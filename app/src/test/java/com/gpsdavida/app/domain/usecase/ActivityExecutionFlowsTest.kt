package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityExecutionFlowsTest {
    private val plannedStart = Instant.parse("2026-01-01T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-01-01T10:00:00Z")
    private val fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:15:00Z"), ZoneOffset.UTC)
    private val repository = RecordingExecutionRepository()
    private val record = RecordActivityExecution(repository)

    @Test
    fun `complete flow persists execution with clock end time`() = runTest {
        CompleteActivityInstance(record, fixedClock)(activity())

        assertEquals(ActivityStatus.DONE, repository.saved!!.status)
        assertEquals(plannedStart, repository.saved!!.actual!!.start)
        assertEquals(fixedClock.instant(), repository.saved!!.actual!!.end)
    }

    @Test
    fun `skip flow persists skipped status`() = runTest {
        SkipActivityInstance(record)(activity())

        assertEquals(ActivityStatus.SKIPPED, repository.saved!!.status)
    }

    @Test
    fun `defer flow persists deferred status`() = runTest {
        DeferActivityInstance(record)(activity())

        assertEquals(ActivityStatus.DEFERRED, repository.saved!!.status)
    }

    private fun activity() = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )

    private class RecordingExecutionRepository : com.superplanner.app.domain.port.ActivityExecutionRepository {
        var saved: ActivityInstance? = null

        override suspend fun save(activity: ActivityInstance) {
            saved = activity
        }

        override suspend fun getById(id: ActivityInstanceId): ActivityExecution? = saved?.let {
            ActivityExecution(it.id, it.status, it.planned, it.actual)
        }

        override fun observeAll(): Flow<List<ActivityExecution>> = flowOf(emptyList())
    }
}
