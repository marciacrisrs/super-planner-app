package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.DailyCapacity
import java.time.Duration

/** Pure policy for a deliberately lighter day. The baseline remains untouched. */
object LowCapacityPlanner {
    private val defaultNormalCapacity = Duration.ofHours(8)
    private const val lowCapacityRatio = 0.5

    fun capacity(normal: DailyCapacity = DailyCapacity(defaultNormalCapacity)): DailyCapacity {
        val reduced = Duration.ofMillis((normal.declared.toMillis() * lowCapacityRatio).toLong())
            .coerceAtLeast(Duration.ofMinutes(30))
        return DailyCapacity(
            normal = normal.normal,
            exceptional = reduced,
            mode = com.superplanner.app.domain.model.CapacityMode.EXCEPTIONAL,
            utilizationLimit = normal.utilizationLimit,
        )
    }
}

private fun Duration.coerceAtLeast(minimum: Duration): Duration =
    if (this < minimum) minimum else this
