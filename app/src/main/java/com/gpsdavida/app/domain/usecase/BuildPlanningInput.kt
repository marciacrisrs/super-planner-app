package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.planning.PlanningInput
import javax.inject.Inject

/** Applies persisted execution state before creating the planning snapshot. */
class BuildPlanningInput @Inject constructor(
    private val applyPersistedExecutions: ApplyPersistedExecutions,
) {
    operator fun invoke(
        activities: List<ActivityInstance>,
        persisted: Map<ActivityInstanceId, ActivityExecution>,
        date: java.time.LocalDate,
        context: NextActionContext,
    ): PlanningInput {
        val prepared = applyPersistedExecutions(activities, persisted)
        val delayedActivity = prepared.firstOrNull { activity ->
            activity.actual?.end?.isAfter(activity.planned.end) == true
        }

        return PlanningInput(
            activities = prepared,
            context = context,
            date = date,
            delayedActivity = delayedActivity,
        )
    }
}
