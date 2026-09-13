package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.MinimumViableDay

/** Keeps explicitly protected essentials ahead of optional work when capacity is reduced. */
object MinimumViableDayPlanner {
    fun order(
        activities: List<ActivityInstance>,
        minimumDay: MinimumViableDay,
    ): List<ActivityInstance> = activities.sortedWith(
        compareByDescending<ActivityInstance> { minimumDay.protects(it) }
            .thenBy { it.priority.weight }
            .thenBy { it.planned.start },
    )
}
