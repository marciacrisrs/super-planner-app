package com.superplanner.app.domain.port

import java.time.Instant

/** Persists explicit user-approved scheduling overrides for generated activity instances. */
interface WeeklyPlanOverrideRepository {
    suspend fun save(overrides: List<WeeklyPlanOverride>)
    suspend fun observe(): List<WeeklyPlanOverride>
}

data class WeeklyPlanOverride(
    val activityId: String,
    val date: String,
    val start: Instant,
    val end: Instant,
)
