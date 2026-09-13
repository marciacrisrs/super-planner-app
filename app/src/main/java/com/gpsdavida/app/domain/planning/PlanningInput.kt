package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.Dependency
import com.superplanner.app.domain.model.NextActionContext
import java.time.Instant

data class PlanningInput(
    val activities: List<ActivityInstance>,
    val context: NextActionContext,
    val recalculationReason: RecalculationReason = RecalculationReason.INITIAL,
    val previousRoute: RouteSnapshot? = null,
) {
    init {
        require(activities.map { it.id }.toSet().size == activities.size) { "Activity ids must be unique" }
        require(context.now != Instant.MIN) { "Planning time must be explicit" }
    }
}

enum class RecalculationReason { INITIAL, TIME_ADVANCED, EXECUTION_CHANGED, ACTIVITY_CHANGED, AVAILABILITY_CHANGED }

data class RouteSnapshot(
    val orderedActivityIds: List<String>,
) 
