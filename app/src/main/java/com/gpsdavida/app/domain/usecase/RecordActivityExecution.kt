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
    suspend fun complete(
        activity: ActivityInstance,
        actualStart: Instant,
        actualEnd: Instant,
    ) {
        repository.save(activity.completed(TimeRange(actualStart, actualEnd)))
    }

    suspend fun skip(activity: ActivityInstance) {
        repository.save(activity.skipped())
    }

    suspend fun defer(activity: ActivityInstance) {
        repository.save(activity.deferred())
    }
}
