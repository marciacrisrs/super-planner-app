package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class NextActionDecision(
    val current: ActivityInstance?,
    val next: ActivityInstance?,
    val travelDurationToNext: Duration = Duration.ZERO,
    val currentReasons: List<NextActionReason> = emptyList(),
    val nextReasons: List<NextActionReason> = emptyList(),
) {
    val recommended: ActivityInstance?
        get() = current ?: next

    val recommendedReasons: List<NextActionReason>
        get() = if (current != null) currentReasons else nextReasons
}

data class NextActionContext(
    val now: Instant,
    val availability: List<Availability> = emptyList(),
    val dependencies: List<Dependency> = emptyList(),
    val currentEnergy: Energy? = null,
    val currentContext: ExecutionContext? = null,
    val currentLocation: LocationId? = null,
    val travelTimes: List<TravelTime> = emptyList(),
    val defaultBuffer: Duration = Duration.ZERO,
    val zoneId: ZoneId = ZoneId.systemDefault(),
)
