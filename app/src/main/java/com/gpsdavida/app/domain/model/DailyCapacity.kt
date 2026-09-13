package com.superplanner.app.domain.model

import java.time.Duration

/**
 * Realistic planning budget for a day.
 *
 * [normal] is the usual capacity. [exceptional] is a day-specific declared
 * override, allowing the same model to represent an unusually lighter or
 * heavier day without changing the user's baseline.
 *
 * [utilizationLimit] intentionally leaves recovery margin instead of treating
 * every free minute as schedulable work.
 */
data class DailyCapacity(
    val normal: Duration,
    val exceptional: Duration? = null,
    val mode: CapacityMode = CapacityMode.NORMAL,
    val utilizationLimit: Double = 0.80,
) {
    init {
        require(!normal.isNegative && !normal.isZero) { "Normal capacity must be positive" }
        require(exceptional == null || (!exceptional.isNegative && !exceptional.isZero)) {
            "Exceptional capacity must be positive when provided"
        }
        require(utilizationLimit in 0.0..1.0) { "Utilization limit must be between 0 and 1" }
        require(mode != CapacityMode.EXCEPTIONAL || exceptional != null) {
            "Exceptional mode requires an exceptional capacity"
        }
    }

    val declared: Duration
        get() = if (mode == CapacityMode.EXCEPTIONAL) exceptional!! else normal

    val schedulable: Duration
        get() = Duration.ofMillis((declared.toMillis() * utilizationLimit).toLong())

    fun remaining(used: Duration): Duration =
        schedulable.minus(used).coerceAtLeast(Duration.ZERO)
}

enum class CapacityMode {
    NORMAL,
    EXCEPTIONAL,
}

private fun Duration.coerceAtLeast(minimum: Duration): Duration =
    if (this < minimum) minimum else this
