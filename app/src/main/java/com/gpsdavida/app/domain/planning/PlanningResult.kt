package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.NextActionReason
import java.time.Duration

data class PlanningResult(
    val route: List<RouteStep>,
    val unscheduled: List<UnscheduledActivity>,
    val decisions: List<PlanningDecision> = emptyList(),
) {
    val isFeasible: Boolean get() = unscheduled.isEmpty()
}

data class RouteStep(
    val activity: ActivityInstance,
    val transitionFromPrevious: Duration = Duration.ZERO,
)

data class UnscheduledActivity(
    val activity: ActivityInstance,
    val reasons: List<PlanningReason>,
)

enum class PlanningReason {
    NO_VALID_WINDOW,
    CONFLICT,
    DEPENDENCY_BLOCKED,
    CAPACITY_EXCEEDED,
    REQUIRED_CONFLICT,
}

data class PlanningDecision(
    val activityId: String,
    val accepted: Boolean,
    val reasons: List<NextActionReason> = emptyList(),
)
