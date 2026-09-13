package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstanceId

/**
 * Recalculates the current route from a fresh immutable planning snapshot.
 * The original plan/activities are never mutated; the PlanningEngine remains the
 * authority for scheduling and trade-offs.
 */
class AdaptiveRouteRecalculator(
    private val planningEngine: PlanningEngine,
) {
    fun recalculate(
        input: PlanningInput,
        reason: RecalculationReason,
    ): RouteRecalculationResult {
        val recalculationInput = input.copy(recalculationReason = reason).snapshot()
        val result = planningEngine(recalculationInput).snapshot()
        val currentRoute = RouteSnapshot(result.route.map { it.activity.id.value })

        return RouteRecalculationResult(
            reason = reason,
            previousRoute = input.previousRoute,
            currentRoute = currentRoute,
            result = result,
            changes = RouteDiff.between(input.previousRoute, currentRoute),
        )
    }
}

data class RouteRecalculationResult(
    val reason: RecalculationReason,
    val previousRoute: RouteSnapshot?,
    val currentRoute: RouteSnapshot,
    val result: PlanningResult,
    val changes: List<RouteChange>,
) {
    val routeChanged: Boolean
        get() = previousRoute != currentRoute

    val hasUnscheduledActivities: Boolean
        get() = result.unscheduled.isNotEmpty()
}

data class RouteChange(
    val activityId: ActivityInstanceId,
    val kind: RouteChangeKind,
    val previousPosition: Int?,
    val currentPosition: Int?,
)

enum class RouteChangeKind {
    ADDED,
    REMOVED,
    MOVED,
}

private object RouteDiff {
    fun between(previous: RouteSnapshot?, current: RouteSnapshot): List<RouteChange> {
        val previousIds = previous?.orderedActivityIds.orEmpty()
        val currentIds = current.orderedActivityIds
        val previousPositions = previousIds.withIndex().associate { it.value to it.index }
        val currentPositions = currentIds.withIndex().associate { it.value to it.index }

        return (previousIds.toSet() + currentIds.toSet())
            .sorted()
            .mapNotNull { id ->
                val before = previousPositions[id]
                val after = currentPositions[id]
                when {
                    before == null -> RouteChange(
                        activityId = ActivityInstanceId(id),
                        kind = RouteChangeKind.ADDED,
                        previousPosition = null,
                        currentPosition = after,
                    )
                    after == null -> RouteChange(
                        activityId = ActivityInstanceId(id),
                        kind = RouteChangeKind.REMOVED,
                        previousPosition = before,
                        currentPosition = null,
                    )
                    before != after -> RouteChange(
                        activityId = ActivityInstanceId(id),
                        kind = RouteChangeKind.MOVED,
                        previousPosition = before,
                        currentPosition = after,
                    )
                    else -> null
                }
            }
    }
}
