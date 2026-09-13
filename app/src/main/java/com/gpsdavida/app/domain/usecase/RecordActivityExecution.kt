package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.ActivityExecutionRepository
import java.time.Instant
import javax.inject.Inject

/** Applies an execution transition and persists the resulting domain state. */
class RecordActivityExecution @Inject constructor(
    private val repository: ActivityExecutionRepository,
) {
    suspend fun start(
        activity: ActivityInstance,
        actualStart: Instant,
    ) {
        repository.save(activity.started(actualStart))
    }

    suspend fun complete(
        activity: ActivityInstance,
        actualEnd: Instant,
    ) {
        val actualStart = activity.actualStart
            ?: repository.getById(activity.id)?.actualStart
            ?: error("Cannot complete an activity without an actual start")
        val runningActivity = activity.copy(
            status = ActivityStatus.IN_PROGRESS,
            actualStart = actualStart,
            actual = null,
        )
        repository.save(runningActivity.completed(TimeRange(actualStart, actualEnd)))
    }

    /** Compatibility entry point for callers that already have a real start and end. */
    suspend fun complete(
        activity: ActivityInstance,
        actualStart: Instant,
        actualEnd: Instant,
    ) {
        require(activity.status == ActivityStatus.PENDING || activity.status == ActivityStatus.IN_PROGRESS) {
            "Only pending or in-progress activities can be completed"
        }
        complete(activity.copy(status = ActivityStatus.IN_PROGRESS, actualStart = actualStart, actual = null), actualEnd)
    }

    suspend fun skip(activity: ActivityInstance) {
        repository.save(activity.skipped())
    }

    suspend fun defer(activity: ActivityInstance) {
        repository.save(activity.deferred())
    }
}
