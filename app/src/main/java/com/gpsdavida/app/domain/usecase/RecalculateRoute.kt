package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.domain.model.ActivityInstance
import com.gpsdavida.app.domain.model.ActivityStatus
import com.gpsdavida.app.domain.model.Availability
import com.gpsdavida.app.domain.model.Dependency
import com.gpsdavida.app.domain.model.DailySchedule
import com.gpsdavida.app.domain.model.Flexibility
import com.gpsdavida.app.domain.model.TravelTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Rebuilds the executable route when the user's reality makes the current plan stale. */
class RecalculateRoute @Inject constructor(
    private val generateDailySchedule: GenerateDailySchedule,
    private val rescheduleAfterDelay: RescheduleAfterDelay,
) {
    operator fun invoke(
        activities: List<ActivityInstance>,
        date: LocalDate,
        availability: List<Availability> = emptyList(),
        dependencies: List<Dependency> = emptyList(),
        defaultBuffer: Duration = Duration.ZERO,
        travelTimes: List<TravelTime> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        delayedActivity: ActivityInstance? = null,
        now: Instant? = null,
    ): DailySchedule {
        val delayed = delayedActivity?.takeIf { it.actual != null && it.actual.end > it.planned.end }
        if (delayed != null) {
            return rescheduleAfterDelay(
                activities = activities,
                delayedActivity = delayed,
                availability = availability,
                dependencies = dependencies,
                defaultBuffer = defaultBuffer,
                travelTimes = travelTimes,
                zoneId = zoneId,
            )
        }

        val relevant = now?.let { current ->
            activities.map { activity ->
                if (activity.status == ActivityStatus.PENDING && activity.planned.end <= current && activity.flexibility != Flexibility.FIXED) {
                    activity.copy(planned = activity.planned.copy(start = current, end = current.plus(activity.plannedDuration)))
                } else {
                    activity
                }
            }
        } ?: activities

        return generateDailySchedule(
            activities = relevant,
            date = date,
            availability = availability,
            dependencies = dependencies,
            defaultBuffer = defaultBuffer,
            travelTimes = travelTimes,
            zoneId = zoneId,
        )
    }
}
