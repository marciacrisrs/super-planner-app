package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalTime

data class TimeRange(
    val start: Instant,
    val end: Instant,
) {
    init {
        require(end > start) { "TimeRange end must be after start" }
    }

    val duration: Duration get() = Duration.between(start, end)
}

data class LocalTimeWindow(
    val start: LocalTime,
    val end: LocalTime,
) {
    init {
        require(end > start) { "LocalTimeWindow end must be after start" }
    }
}
