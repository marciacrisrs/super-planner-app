package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface ActivityExecutionRepository {
    /** Production implementations should override this with an atomic conditional transition. */
    suspend fun startIfPending(activity: ActivityInstance, actualStart: Instant): Boolean {
        if (getById(activity.id)?.status != null) return false
        save(activity.started(actualStart))
        return true
    }

    /** Production implementations should override this with an atomic conditional transition. */
    suspend fun completeIfInProgress(id: ActivityInstanceId, actualEnd: Instant): ActivityExecution? {
        val execution = getById(id) ?: return null
        if (execution.status.name != "IN_PROGRESS") return null
        val start = execution.actualStart ?: return null
        val completed = execution.copy(
            status = com.superplanner.app.domain.model.ActivityStatus.DONE,
            actual = com.superplanner.app.domain.model.TimeRange(start, actualEnd),
        )
        val activity = ActivityInstance(
            id = execution.activityInstanceId,
            source = com.superplanner.app.domain.model.ActivitySource.FromTask(com.superplanner.app.domain.model.TaskId("execution-${id.value}")),
            flexibility = com.superplanner.app.domain.model.Flexibility.FLEXIBLE,
            planned = execution.planned,
            actualStart = start,
            actual = completed.actual,
            status = completed.status,
        )
        save(activity)
        return completed
    }

    suspend fun save(activity: ActivityInstance)

    suspend fun getById(id: ActivityInstanceId): ActivityExecution?

    fun observeAll(): Flow<List<ActivityExecution>>
}
