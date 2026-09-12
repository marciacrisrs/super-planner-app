package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import javax.inject.Inject

/** Overlays persisted execution state onto freshly materialized activities. */
class ApplyPersistedExecutions @Inject constructor() {
    operator fun invoke(
        activities: List<ActivityInstance>,
        persisted: Map<ActivityInstanceId, ActivityExecution>,
    ): List<ActivityInstance> = activities.map { activity ->
        val execution = persisted[activity.id] ?: return@map activity
        activity.copy(
            status = execution.status,
            actual = execution.actual,
        )
    }
}
