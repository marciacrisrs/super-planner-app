package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.LocalTimeWindow
import java.time.LocalTime
import javax.inject.Inject

/**
 * Resolves the effective free windows for a day by subtracting BLOCKED windows
 * from FREE windows. Kept independent from Android/Room so the planner can use it.
 */
class ResolveAvailableWindows @Inject constructor() {
    operator fun invoke(rules: List<Availability>): List<LocalTimeWindow> {
        val free = rules.filter { it.kind == AvailabilityKind.FREE }.map { it.window }
        val blocked = rules.filter { it.kind == AvailabilityKind.BLOCKED }.map { it.window }

        return free.flatMap { window ->
            var segments = listOf(window)
            blocked.forEach { exclusion ->
                segments = segments.flatMap { segment -> subtract(segment, exclusion) }
            }
            segments
        }.sortedBy { it.start }
    }

    private fun subtract(source: LocalTimeWindow, exclusion: LocalTimeWindow): List<LocalTimeWindow> {
        if (exclusion.end <= source.start || exclusion.start >= source.end) return listOf(source)

        val result = buildList {
            if (source.start < exclusion.start) {
                add(LocalTimeWindow(source.start, minOf(exclusion.start, source.end)))
            }
            if (exclusion.end < source.end) {
                add(LocalTimeWindow(maxOf(exclusion.end, source.start), source.end))
            }
        }
        return result.filter { it.start < it.end }
    }
}
