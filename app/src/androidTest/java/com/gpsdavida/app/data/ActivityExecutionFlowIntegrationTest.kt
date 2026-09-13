package com.superplanner.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.superplanner.app.data.local.SuperPlannerDatabase
import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.usecase.ApplyPersistedExecutions
import com.superplanner.app.domain.usecase.RecordActivityExecution
import com.superplanner.app.domain.usecase.DeferActivityInstance
import com.superplanner.app.domain.usecase.SkipActivityInstance
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityExecutionFlowIntegrationTest {
    private lateinit var database: SuperPlannerDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SuperPlannerDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `start complete skip and defer survive repository recreation`() = runTest {
        val activity = sampleActivity("activity-complete")
        val actualStart = Instant.parse("2026-01-01T09:15:00Z")
        val actualEnd = Instant.parse("2026-01-01T10:20:00Z")
        val repository = freshRepository()
        val record = RecordActivityExecution(repository)

        record.start(activity, actualStart)
        val restartedRepository = freshRepository()
        assertEquals(ActivityStatus.IN_PROGRESS, restartedRepository.getById(activity.id)?.status)
        assertEquals(actualStart, restartedRepository.getById(activity.id)?.actualStart)

        RecordActivityExecution(restartedRepository).complete(activity, actualEnd)
        assertRecovered(activity.id, ActivityStatus.DONE, actualStart, actualEnd)

        val skipped = sampleActivity("activity-skip")
        skipThroughFreshRepository(skipped)
        assertRecovered(skipped.id, ActivityStatus.SKIPPED, null, null)

        val deferred = sampleActivity("activity-defer")
        deferThroughFreshRepository(deferred)
        assertRecovered(deferred.id, ActivityStatus.DEFERRED, null, null)
    }

    @Test
    fun `observeAll returns persisted executions for overlay`() = runTest {
        val repository = freshRepository()
        val record = RecordActivityExecution(repository)
        val activity = sampleActivity("activity-overlay")
        val actualStart = Instant.parse("2026-01-01T09:10:00Z")
        val actualEnd = Instant.parse("2026-01-01T10:40:00Z")

        record.start(activity, actualStart)
        record.complete(activity, actualEnd)

        val recreated = freshRepository()
        val persisted = recreated.observeAll().first().associateBy { it.activityInstanceId }
        val merged = ApplyPersistedExecutions()(listOf(activity), persisted).single()

        assertEquals(ActivityStatus.DONE, merged.status)
        assertEquals(actualStart, merged.actualStart)
        assertEquals(TimeRange(actualStart, actualEnd), merged.actual)
    }

    @Test
    fun `in-progress execution overlays onto a freshly scheduled activity`() = runTest {
        val repository = freshRepository()
        val record = RecordActivityExecution(repository)
        val activity = sampleActivity("activity-running")
        val actualStart = Instant.parse("2026-01-01T09:25:00Z")

        record.start(activity, actualStart)

        val persisted = freshRepository().observeAll().first().associateBy { it.activityInstanceId }
        val merged = ApplyPersistedExecutions()(listOf(activity), persisted).single()

        assertEquals(ActivityStatus.IN_PROGRESS, merged.status)
        assertEquals(actualStart, merged.actualStart)
        assertNull(merged.actual)
    }

    private suspend fun skipThroughFreshRepository(activity: ActivityInstance) {
        SkipActivityInstance(RecordActivityExecution(freshRepository())).invoke(activity)
    }

    private suspend fun deferThroughFreshRepository(activity: ActivityInstance) {
        DeferActivityInstance(RecordActivityExecution(freshRepository())).invoke(activity)
    }

    private suspend fun assertRecovered(
        id: ActivityInstanceId,
        status: ActivityStatus,
        actualStart: Instant?,
        actualEnd: Instant?,
    ) {
        val loaded = freshRepository().getById(id)
        requireNotNull(loaded)
        assertEquals(status, loaded.status)
        assertEquals(actualStart, loaded.actualStart)
        if (actualStart != null && actualEnd != null) {
            assertEquals(TimeRange(actualStart, actualEnd), loaded.actual)
        } else {
            assertNull(loaded.actual)
        }
    }

    private fun freshRepository() = RoomActivityExecutionRepository(database.activityExecutionDao())

    private fun sampleActivity(id: String) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(
            Instant.parse("2026-01-01T09:00:00Z"),
            Instant.parse("2026-01-01T10:00:00Z"),
        ),
    )
}
