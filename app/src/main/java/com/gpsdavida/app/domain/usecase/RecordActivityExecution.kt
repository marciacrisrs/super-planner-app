package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
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
        repository.save(activity.copy(status = com.superplanner.app.domain.model.ActivityStatus.IN_PROGRESS, actualStart = actualStart).completed(TimeRange(actualStart, actualEnd)))
    }

    suspend fun skip(activity: ActivityInstance) {
        repository.save(activity.skipped())
    }

    suspend fun defer(activity: ActivityInstance) {
        repository.save(activity.deferred())
    }
}
