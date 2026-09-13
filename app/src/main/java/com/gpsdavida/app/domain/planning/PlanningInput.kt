package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.NextActionContext

/** Immutable snapshot consumed by a PlanningEngine. It is safe to reuse for recalculation. */
data class PlanningInput(
    val activities: List<ActivityInstance>,
    val context: NextActionContext,
    val recalculationReason: RecalculationReason = RecalculationReason.INITIAL,
    val previousRoute: RouteSnapshot? = null,
) {
    init {
        require(activities.map { it.id }.distinct().size == activities.size) {
            "Activity ids must be unique"
        }
    }

    fun snapshot(): PlanningInput = copy(
        activities = activities.toList(),
        previousRoute = previousRoute?.copy(orderedActivityIds = previousRoute.orderedActivityIds.toList()),
    )
}

enum class RecalculationReason {
    INITIAL,
    TIME_ADVANCED,
    EXECUTION_CHANGED,
    ACTIVITY_CHANGED,
    AVAILABILITY_CHANGED,
}

data class RouteSnapshot(
    val orderedActivityIds: List<String>,
)
