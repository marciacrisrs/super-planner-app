package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.Dependency
import com.superplanner.app.domain.model.DailySchedule
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TravelTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Rebuilds the pending part of the day when a real execution overruns its plan. */
class RescheduleAfterDelay @Inject constructor(
    private val generateDailySchedule: GenerateDailySchedule,
) {
    operator fun invoke(
        activities: List<ActivityInstance>,
        delayedActivity: ActivityInstance,
        availability: List<Availability> = emptyList(),
        dependencies: List<Dependency> = emptyList(),
        defaultBuffer: Duration = Duration.ZERO,
        travelTimes: List<TravelTime> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): DailySchedule {
        require(delayedActivity.status == ActivityStatus.DONE) {
            "Only completed activities can trigger delay rescheduling"
        }
        val actual = delayedActivity.actual
            ?: error("Completed activity must have an actual execution interval")
        require(actual.end > delayedActivity.planned.end) {
            "Only executions that finish after the planned end can trigger rescheduling"
        }

        val date = actual.end.atZone(zoneId).toLocalDate()
        val shifted = activities.map { activity ->
            if (
                activity.status == ActivityStatus.PENDING &&
                activity.flexibility == Flexibility.FLEXIBLE &&
                activity.planned.start < actual.end &&
                activity.planned.start.atZone(zoneId).toLocalDate() == date
            ) {
                val start = actual.end
                activity.copy(
                    planned = activity.planned.copy(
                        start = start,
                        end = start.plus(activity.plannedDuration),
                    ),
                )
            } else {
                activity
            }
        }

        return generateDailySchedule(
            activities = shifted,
            date = date,
            availability = availability,
            dependencies = dependencies,
            defaultBuffer = defaultBuffer,
            travelTimes = travelTimes,
            zoneId = zoneId,
        )
    }

    private val ActivityInstance.actualEnd: Instant
        get() = requireNotNull(actual).end
}
