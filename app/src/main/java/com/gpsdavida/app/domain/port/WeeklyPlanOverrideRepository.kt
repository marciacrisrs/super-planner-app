package com.superplanner.app.domain.port

import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Persists explicit user-approved scheduling overrides for generated activity instances. */
interface WeeklyPlanOverrideRepository {
    suspend fun save(overrides: List<WeeklyPlanOverride>)
    fun observe(): Flow<List<WeeklyPlanOverride>>
}

data class WeeklyPlanOverride(
    val activityId: String,
    val date: String,
    val start: Instant,
    val end: Instant,
)
