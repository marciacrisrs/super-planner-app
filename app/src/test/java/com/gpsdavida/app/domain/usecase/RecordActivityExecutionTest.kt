package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.ActivityExecutionRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordActivityExecutionTest {
    private val plannedStart = Instant.parse("2026-01-01T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-01-01T10:00:00Z")
    private val repository = FakeActivityExecutionRepository()
    private val useCase = RecordActivityExecution(repository)

    @Test
    fun `start persists actual start without inventing actual end`() = runTest {
        val actualStart = Instant.parse("2026-01-01T09:15:00Z")

        useCase.start(activity(), actualStart)

        val result = repository.saved!!
        assertEquals(ActivityStatus.IN_PROGRESS, result.status)
        assertEquals(actualStart, result.actualStart)
        assertNull(result.actual)
    }

    @Test
    fun `complete persists actual time and duration from real start`() = runTest {
        val actualStart = Instant.parse("2026-01-01T09:15:00Z")
        val actualEnd = Instant.parse("2026-01-01T10:45:00Z")
        val started = activity().started(actualStart)

        useCase.start(activity(), actualStart)
        useCase.complete(started, actualEnd)

        val result = repository.saved!!
        assertEquals(ActivityStatus.DONE, result.status)
        assertEquals(actualStart, result.actualStart)
        assertEquals(TimeRange(actualStart, actualEnd), result.actual)
        assertEquals(Duration.ofMinutes(90), result.actualDuration)
        assertEquals(Duration.ofMinutes(30), result.durationVariance)
    }

    @Test
    fun `complete can recover real start from repository`() = runTest {
        val actualStart = Instant.parse("2026-01-01T09:15:00Z")
        val actualEnd = Instant.parse("2026-01-01T10:45:00Z")
        useCase.start(activity(), actualStart)

        useCase.complete(activity(), actualEnd)

        assertEquals(TimeRange(actualStart, actualEnd), repository.saved!!.actual)
    }

    @Test
    fun `complete with explicit real start preserves it for legacy callers`() = runTest {
        val actualStart = Instant.parse("2026-01-01T09:20:00Z")
        val actualEnd = Instant.parse("2026-01-01T10:50:00Z")

        useCase.complete(activity(), actualStart, actualEnd)

        assertEquals(ActivityStatus.DONE, repository.saved!!.status)
        assertEquals(TimeRange(actualStart, actualEnd), repository.saved!!.actual)
    }

    @Test
    fun `skip persists status without inventing execution time`() = runTest {
        useCase.skip(activity())

        assertEquals(ActivityStatus.SKIPPED, repository.saved!!.status)
        assertNull(repository.saved!!.actual)
        assertNull(repository.saved!!.actualStart)
    }

    @Test
    fun `defer persists status without inventing execution time`() = runTest {
        useCase.defer(activity())

        assertEquals(ActivityStatus.DEFERRED, repository.saved!!.status)
        assertNull(repository.saved!!.actual)
        assertNull(repository.saved!!.actualStart)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot start an activity that is not pending`() = runTest {
        useCase.start(activity().started(plannedStart), plannedStart)
    }

    @Test(expected = IllegalStateException::class)
    fun `cannot complete an activity without an actual start`() = runTest {
        useCase.complete(activity(), plannedEnd)
    }

    private fun activity() = ActivityInstance(
        id = com.superplanner.app.domain.model.ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )

    private class FakeActivityExecutionRepository : ActivityExecutionRepository {
        var saved: ActivityInstance? = null

        override suspend fun save(activity: ActivityInstance) {
            saved = activity
        }

        override suspend fun getById(id: com.superplanner.app.domain.model.ActivityInstanceId): ActivityExecution? = saved?.let {
            ActivityExecution(it.id, it.status, it.planned, it.actualStart, it.actual)
        }

        override fun observeAll(): Flow<List<ActivityExecution>> = flowOf(
            saved?.let { ActivityExecution(it.id, it.status, it.planned, it.actualStart, it.actual) }
                ?.let { listOf(it) }
                .orEmpty(),
        )
    }
}
