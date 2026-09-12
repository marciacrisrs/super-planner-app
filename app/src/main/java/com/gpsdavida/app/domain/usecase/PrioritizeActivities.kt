package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Priority
import javax.inject.Inject

/**
 * Applies the single priority ordering used by the planner.
 * Kotlin's sortedWith is stable, so activities with equal priority keep their input order.
 */
class PrioritizeActivities @Inject constructor() {
    operator fun <T> invoke(items: List<T>, priorityOf: (T) -> Priority): List<T> =
        items.sortedWith(compareBy { priorityOf(it).weight })
}

val Priority.weight: Int
    get() = when (this) {
        Priority.REQUIRED -> 0
        Priority.IMPORTANT -> 1
        Priority.DESIRABLE -> 2
        Priority.LEISURE -> 3
    }

/** True when the priority policy considers the item non-discardable by ordinary rescheduling. */
val Priority.isMandatory: Boolean
    get() = this == Priority.REQUIRED
