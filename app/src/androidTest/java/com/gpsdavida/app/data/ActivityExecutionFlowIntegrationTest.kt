package com.superplanner.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.superplanner.app.data.local.SuperPlannerDatabase
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.usecase.ApplyPersistedExecutions
import com.superplanner.app.domain.usecase.DeferActivityInstance
import com.superplanner.app.domain.usecase.RecordActivityExecution
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
    fun `complete skip and defer survive repository recreation`() = runTest {
        val activity = sampleActivity("activity-complete")
        completeThroughFreshRepository(activity)
        assertRecovered(activity.id, ActivityStatus.DONE)

        val skipped = sampleActivity("activity-skip")
        skipThroughFreshRepository(skipped)
        assertRecovered(skipped.id, ActivityStatus.SKIPPED)

        val deferred = sampleActivity("activity-defer")
        deferThroughFreshRepository(deferred)
        assertRecovered(deferred.id, ActivityStatus.DEFERRED)
    }

    @Test
    fun `observeAll returns persisted executions for overlay`() = runTest {
        val repository = RoomActivityExecutionRepository(database.activityExecutionDao())
        val record = RecordActivityExecution(repository)
        val activity = sampleActivity("activity-overlay")
        val completed = activity.completed(
            TimeRange(
                Instant.parse("2026-01-01T09:00:00Z"),
                Instant.parse("2026-01-01T09:40:00Z"),
            ),
        )

        record.complete(activity, completed.actual!!.start, completed.actual.end)

        val recreated = RoomActivityExecutionRepository(database.activityExecutionDao())
        val persisted = recreated.observeAll().first().associateBy { it.activityInstanceId }
        val merged = ApplyPersistedExecutions()(listOf(activity), persisted).single()

        assertEquals(ActivityStatus.DONE, merged.status)
        assertEquals(completed.actual, merged.actual)
    }

    private suspend fun completeThroughFreshRepository(activity: ActivityInstance) {
        val repository = RoomActivityExecutionRepository(database.activityExecutionDao())
        RecordActivityExecution(repository).complete(
            activity,
            activity.planned.start,
            Instant.parse("2026-01-01T10:00:00Z"),
        )
    }

    private suspend fun skipThroughFreshRepository(activity: ActivityInstance) {
        SkipActivityInstance(RecordActivityExecution(freshRepository())).invoke(activity)
    }

    private suspend fun deferThroughFreshRepository(activity: ActivityInstance) {
        DeferActivityInstance(RecordActivityExecution(freshRepository())).invoke(activity)
    }

    private suspend fun assertRecovered(id: ActivityInstanceId, status: ActivityStatus) {
        val loaded = freshRepository().getById(id)
        requireNotNull(loaded)
        assertEquals(status, loaded.status)
        if (status == ActivityStatus.DONE) {
            assertEquals(Instant.parse("2026-01-01T10:00:00Z"), loaded.actual?.end)
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
