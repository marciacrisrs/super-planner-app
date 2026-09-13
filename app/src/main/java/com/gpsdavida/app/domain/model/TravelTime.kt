package com.superplanner.app.domain.model

import java.time.Duration

data class TravelTime(
    val from: LocationId,
    val to: LocationId,
    val duration: Duration,
)
