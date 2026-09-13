package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailySchedule
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.ScheduleConflictReason
import com.superplanner.app.domain.usecase.GenerateDailySchedule
import com.superplanner.app.domain.usecase.RescheduleAfterDelay
import java.time.LocalDate
import javax.inject.Inject

/**
 * Production PlanningEngine backed by the existing deterministic scheduling rules.
 * Capacity and learned durations are passed through the same boundary used by Agora.
 */
class DefaultPlanningEngine @Inject constructor(
    private val generateDailySchedule: GenerateDailySchedule,
    private val rescheduleAfterDelay: RescheduleAfterDelay,
) : PlanningEngine {
    override fun invoke(input: PlanningInput): PlanningResult {
        val delayed = input.delayedActivity?.takeIf {
            it.actual != null && it.actual.end > it.planned.end
        }

        val schedule = delayed?.let {
            rescheduleAfterDelay(
                activities = input.activities,
                delayedActivity = it,
                availability = input.context.availability,
                dependencies = input.context.dependencies,
                defaultBuffer = input.context.defaultBuffer,
                travelTimes = input.context.travelTimes,
                zoneId = input.context.zoneId,
            )
        } ?: generateForCurrentTime(input, input.date)

        return schedule.toPlanningResult()
    }

    private fun generateForCurrentTime(input: PlanningInput, date: LocalDate): DailySchedule {
        val relevant = input.activities.map { activity ->
            if (
                activity.status == ActivityStatus.PENDING &&
                activity.planned.end <= input.context.now &&
                activity.flexibility != Flexibility.FIXED
            ) {
                activity.copy(
                    planned = activity.planned.copy(
                        start = input.context.now,
                        end = input.context.now.plus(
                            input.context.learnedDurations[activity.id] ?: activity.plannedDuration,
                        ),
                    ),
                )
            } else {
                activity
            }
        }

        return generateDailySchedule(
            activities = relevant,
            date = date,
            availability = input.context.availability,
            dependencies = input.context.dependencies,
            defaultBuffer = input.context.defaultBuffer,
            travelTimes = input.context.travelTimes,
            zoneId = input.context.zoneId,
            dailyCapacity = input.context.dailyCapacity,
            learnedDurations = input.context.learnedDurations,
        )
    }

    private fun DailySchedule.toPlanningResult(): PlanningResult {
        val route = activities.map { activity ->
            RouteStep(
                activity = activity,
                start = activity.planned.start,
                end = activity.planned.end,
            )
        }
        val unscheduled = conflicts.map { conflict ->
            UnscheduledActivity(
                activity = conflict.activity,
                reasons = listOf(conflict.reason.toPlanningReason()),
            )
        }
        val decisions = buildList {
            route.forEach { step ->
                add(
                    PlanningDecision(
                        activityId = step.activity.id.value,
                        outcome = PlanningDecisionOutcome.SCHEDULED,
                        reasons = listOf(PlanningReason.PRIORITY_PRESERVED),
                    ),
                )
            }
            unscheduled.forEach { item ->
                add(
                    PlanningDecision(
                        activityId = item.activity.id.value,
                        outcome = PlanningDecisionOutcome.BLOCKED,
                        reasons = item.reasons,
                    ),
                )
            }
        }

        return PlanningResult(
            route = route,
            unscheduled = unscheduled,
            decisions = decisions,
        ).snapshot()
    }

    private fun ScheduleConflictReason.toPlanningReason(): PlanningReason = when (this) {
        ScheduleConflictReason.FIXED_OVERLAP -> PlanningReason.REQUIRED_CONFLICT
        ScheduleConflictReason.NO_AVAILABLE_WINDOW -> PlanningReason.NO_VALID_WINDOW
        ScheduleConflictReason.DEPENDENCY_NOT_SATISFIED -> PlanningReason.DEPENDENCY_BLOCKED
        ScheduleConflictReason.CAPACITY_EXCEEDED -> PlanningReason.CAPACITY_EXCEEDED
    }
}
