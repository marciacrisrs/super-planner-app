package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.ActivityExecutionRepository
import java.time.Instant
import javax.inject.Inject

/** Applies execution transitions without allowing stale caller snapshots to overwrite state. */
class RecordActivityExecution @Inject constructor(
    private val repository: ActivityExecutionRepository,
) {
    suspend fun start(
        activity: ActivityInstance,
        actualStart: Instant,
    ): ActivityInstance {
        require(activity.status == ActivityStatus.PENDING) { "Only pending activities can be started" }
        check(repository.startIfPending(activity, actualStart)) { "Activity is no longer pending" }
        return activity.started(actualStart)
    }

    suspend fun complete(
        activity: ActivityInstance,
        actualEnd: Instant,
    ): ActivityInstance {
        val execution = repository.completeIfInProgress(activity.id, actualEnd)
            ?: error("Cannot complete an activity that is not in progress")
        val actualStart = execution.actualStart
            ?: error("Completed execution must have an actual start")
        return activity.copy(
            status = ActivityStatus.IN_PROGRESS,
            actualStart = actualStart,
            actual = null,
        ).completed(TimeRange(actualStart, actualEnd))
    }

    /** Compatibility entry point for callers that already have a real start and end. */
    suspend fun complete(
        activity: ActivityInstance,
        actualStart: Instant,
        actualEnd: Instant,
    ): ActivityInstance {
        if (activity.status == ActivityStatus.PENDING) {
            start(activity, actualStart)
        } else {
            require(activity.status == ActivityStatus.IN_PROGRESS) {
                "Only pending or in-progress activities can be completed"
            }
        }
        val persisted = repository.getById(activity.id)
        if (persisted?.actualStart != actualStart) {
            error("Actual start does not match the persisted execution")
        }
        return complete(activity.copy(status = ActivityStatus.IN_PROGRESS, actualStart = actualStart, actual = null), actualEnd)
    }

    suspend fun skip(activity: ActivityInstance) {
        require(activity.status == ActivityStatus.PENDING) { "Only pending activities can be skipped" }
        repository.save(activity.skipped())
    }

    suspend fun defer(activity: ActivityInstance) {
        require(activity.status == ActivityStatus.PENDING) { "Only pending activities can be deferred" }
        repository.save(activity.deferred())
    }
}
