package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.DailySchedule
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.ScheduleConflict
import com.superplanner.app.domain.model.ScheduleConflictReason
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.planning.PlanningInput
import com.superplanner.app.domain.planning.RecalculationReason
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Compatibility adapter: all route calculation is delegated to the PlanningEngine. */
class RecalculateRoute @Inject constructor(
    private val planningEngine: PlanningEngine,
) {
    operator fun invoke(
        activities: List<ActivityInstance>,
        date: java.time.LocalDate,
        availability: List<com.superplanner.app.domain.model.Availability> = emptyList(),
        dependencies: List<com.superplanner.app.domain.model.Dependency> = emptyList(),
        defaultBuffer: Duration = Duration.ZERO,
        travelTimes: List<com.superplanner.app.domain.model.TravelTime> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        delayedActivity: ActivityInstance? = null,
        now: Instant? = null,
        dailyCapacity: DailyCapacity? = null,
    ): DailySchedule {
        val current = now ?: activities.firstOrNull()?.planned?.start ?: Instant.EPOCH
        val result = planningEngine(
            PlanningInput(
                activities = activities,
                context = NextActionContext(
                    now = current,
                    availability = availability,
                    dependencies = dependencies,
                    travelTimes = travelTimes,
                    defaultBuffer = defaultBuffer,
                    dailyCapacity = dailyCapacity,
                    zoneId = zoneId,
                ),
                date = date,
                recalculationReason = if (delayedActivity != null) RecalculationReason.EXECUTION_CHANGED else if (now != null) RecalculationReason.TIME_ADVANCED else RecalculationReason.ACTIVITY_CHANGED,
                delayedActivity = delayedActivity,
            ),
        )

        return DailySchedule(
            activities = result.route.map { it.activity },
            conflicts = result.unscheduled.map { item ->
                ScheduleConflict(
                    activity = item.activity,
                    reason = item.reasons.firstOrNull()?.toScheduleConflictReason() ?: ScheduleConflictReason.NO_AVAILABLE_WINDOW,
                )
            },
        )
    }

    private fun com.superplanner.app.domain.planning.PlanningReason.toScheduleConflictReason(): ScheduleConflictReason = when (this) {
        com.superplanner.app.domain.planning.PlanningReason.REQUIRED_CONFLICT -> ScheduleConflictReason.FIXED_OVERLAP
        com.superplanner.app.domain.planning.PlanningReason.DEPENDENCY_BLOCKED -> ScheduleConflictReason.DEPENDENCY_NOT_SATISFIED
        else -> ScheduleConflictReason.NO_AVAILABLE_WINDOW
    }
}
