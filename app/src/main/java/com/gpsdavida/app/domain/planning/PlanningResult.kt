package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import java.time.Duration
import java.time.Instant

/** Complete result of a planning calculation: route plus explicit trade-offs. */
data class PlanningResult(
    val route: List<RouteStep>,
    val unscheduled: List<UnscheduledActivity>,
    val decisions: List<PlanningDecision> = emptyList(),
) {
    val isFeasible: Boolean get() = unscheduled.isEmpty()

    fun snapshot(): PlanningResult = copy(
        route = route.toList(),
        unscheduled = unscheduled.map { it.copy(reasons = it.reasons.toList()) },
        decisions = decisions.map { it.copy(reasons = it.reasons.toList()) },
    )
}

data class RouteStep(
    val activity: ActivityInstance,
    val start: Instant = activity.planned.start,
    val end: Instant = activity.planned.end,
    val transitionFromPrevious: Duration = Duration.ZERO,
)

data class UnscheduledActivity(
    val activity: ActivityInstance,
    val reasons: List<PlanningReason>,
)

data class PlanningDecision(
    val activityId: String,
    val outcome: PlanningDecisionOutcome,
    val reasons: List<PlanningReason>,
)

enum class PlanningDecisionOutcome {
    SCHEDULED,
    DEFERRED,
    BLOCKED,
}

enum class PlanningReason {
    NO_VALID_WINDOW,
    CONFLICT,
    DEPENDENCY_BLOCKED,
    CAPACITY_EXCEEDED,
    REQUIRED_CONFLICT,
    PRIORITY_PRESERVED,
}
