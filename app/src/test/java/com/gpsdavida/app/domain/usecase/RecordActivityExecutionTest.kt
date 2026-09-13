package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.ActivityExecutionRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordActivityExecutionTest {
    private val plannedStart = Instant.parse("2026-01-01T09:00:00Z")
    private val plannedEnd = Instant.parse("2026-01-01T10:00:00Z")
    private val repository = FakeActivityExecutionRepository()
    private val useCase = RecordActivityExecution(repository)

    @Test
    fun `complete persists actual time and duration`() = runTest {
        val actualEnd = Instant.parse("2026-01-01T10:30:00Z")

        useCase.complete(activity(), plannedStart, actualEnd)

        val result = repository.saved!!
        assertEquals(ActivityStatus.DONE, result.status)
        assertEquals(TimeRange(plannedStart, actualEnd), result.actual)
        assertEquals(Duration.ofMinutes(90), result.actualDuration)
        assertEquals(Duration.ofMinutes(30), result.durationVariance)
    }

    @Test
    fun `skip persists status without inventing execution time`() = runTest {
        useCase.skip(activity())

        assertEquals(ActivityStatus.SKIPPED, repository.saved!!.status)
        assertNull(repository.saved!!.actual)
    }

    @Test
    fun `defer persists status without inventing execution time`() = runTest {
        useCase.defer(activity())

        assertEquals(ActivityStatus.DEFERRED, repository.saved!!.status)
        assertNull(repository.saved!!.actual)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot complete an activity that is not pending`() = runTest {
        useCase.complete(activity().skipped(), plannedStart, plannedEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot skip an activity that is not pending`() = runTest {
        useCase.skip(activity().completed(TimeRange(plannedStart, plannedEnd)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot defer an activity that is not pending`() = runTest {
        useCase.defer(activity().completed(TimeRange(plannedStart, plannedEnd)))
    }

    private fun activity() = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(plannedStart, plannedEnd),
    )

    private class FakeActivityExecutionRepository : ActivityExecutionRepository {
        var saved: ActivityInstance? = null

        override suspend fun save(activity: ActivityInstance) {
            saved = activity
        }

        override suspend fun getById(id: ActivityInstanceId): ActivityExecution? = saved?.let {
            ActivityExecution(it.id, it.status, it.planned, it.actual)
        }

        override fun observeAll(): Flow<List<ActivityExecution>> =
            kotlinx.coroutines.flow.flowOf(
                saved?.let { ActivityExecution(it.id, it.status, it.planned, it.actual) }
                    ?.let { listOf(it) }
                    .orEmpty(),
            )
    }
}
