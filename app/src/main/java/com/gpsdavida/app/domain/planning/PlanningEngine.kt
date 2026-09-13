package com.superplanner.app.domain.planning

/**
 * Domain boundary for converting a complete planning snapshot into a route.
 * Implementations must be deterministic for the same input and must not mutate it.
 */
fun interface PlanningEngine {
    operator fun invoke(input: PlanningInput): PlanningResult
}
