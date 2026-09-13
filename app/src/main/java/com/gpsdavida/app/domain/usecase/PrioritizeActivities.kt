package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import java.time.Instant
import javax.inject.Inject

/**
 * Applies the planner's priority policy.
 *
 * Protected anchors (fixed activities) are considered before flexible work.
 * Within flexible work, explicit priority wins; due dates and consequences of delay
 * refine the ordering without allowing secondary factors to silently override priority.
 * Kotlin's sortedWith is stable, so equal keys preserve input order.
 */
class PrioritizeActivities @Inject constructor() {
    operator fun <T> invoke(items: List<T>, priorityOf: (T) -> Priority): List<T> =
        items.sortedWith(compareBy { priorityOf(it).weight })

    operator fun invoke(items: List<ActivityInstance>): List<ActivityInstance> =
        items.sortedWith(
            compareByDescending<ActivityInstance> { it.flexibility == Flexibility.FIXED }
                .thenBy { it.priority.weight }
                .thenBy { it.dueAt ?: Instant.MAX }
                .thenByDescending { it.delayConsequence.weight },
        )
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
