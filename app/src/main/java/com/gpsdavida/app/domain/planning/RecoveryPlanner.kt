package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Recovers work from missed days without treating every pending item as mandatory backlog.
 * The planner only redistributes within a bounded horizon and preserves existing future work.
 */
class RecoveryPlanner @Inject constructor() {
    operator fun invoke(
        activities: List<ActivityInstance>,
        recoveryStart: LocalDate,
        capacities: Map<LocalDate, DailyCapacity>,
        horizonDays: Int = 7,
    ): RecoveryPlan {
        require(horizonDays > 0) { "Recovery horizon must be positive" }

        val horizon = (0 until horizonDays).map { recoveryStart.plusDays(it.toLong()) }
        val pendingRecovery = activities
            .asSequence()
            .filter { it.status == ActivityStatus.PENDING || it.status == ActivityStatus.DEFERRED }
            .map { RecoveryItem(it, it.planned.start.atZone(java.time.ZoneOffset.UTC).toLocalDate()) }
            .filter { it.originalDate.isBefore(recoveryStart) }
            .distinctBy { it.activity.id }
            .toList()

        val future = activities.filter {
            it.status == ActivityStatus.PENDING &&
                !it.planned.start.atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(recoveryStart)
        }

        val committed = horizon.associateWith { date ->
            future.filter { it.flexibility == Flexibility.FIXED && localDate(it) == date }
                .fold(Duration.ZERO) { total, activity -> total.plus(activity.plannedDuration) }
        }.toMutableMap()

        val capacityByDay = linkedMapOf<LocalDate, RecoveryCapacity>()
        val assignments = mutableListOf<RecoveryAssignment>()
        val reassess = mutableListOf<RecoveryItem>()

        val ordered = pendingRecovery.sortedWith(
            compareBy<RecoveryItem> { deadlineRank(it.activity, recoveryStart) }
                .thenBy { it.activity.priority.weight }
                .thenByDescending { it.activity.delayConsequence.weight }
                .thenBy { it.activity.planned.start },
        )

        for (item in ordered) {
            val reason = reasonFor(item.activity, recoveryStart)
            val candidate = horizon.firstOrNull { date ->
                val capacity = capacities[date] ?: return@firstOrNull false
                if (item.activity.dueAt != null && item.activity.dueAt!!.atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(date)) {
                    return@firstOrNull false
                }
                val used = committed.getValue(date)
                capacity.remaining(used) >= item.activity.plannedDuration
            }

            if (candidate != null && shouldRedistribute(item.activity)) {
                committed[candidate] = committed.getValue(candidate).plus(item.activity.plannedDuration)
                assignments += RecoveryAssignment(item, candidate, RecoveryAction.REDISTRIBUTE, reason)
            } else {
                assignments += RecoveryAssignment(item, recoveryStart, RecoveryAction.REASSESS, reason)
                reassess += item
            }
        }

        horizon.forEach { date ->
            val capacity = capacities[date] ?: return@forEach
            val used = committed.getValue(date)
            capacityByDay[date] = RecoveryCapacity(
                date = date,
                capacity = capacity,
                committed = used,
                remaining = capacity.remaining(used),
            )
        }

        return RecoveryPlan(assignments, reassess, capacityByDay)
    }

    private fun shouldRedistribute(activity: ActivityInstance): Boolean =
        activity.priority.weight <= 1 ||
            activity.dueAt != null ||
            activity.delayConsequence.weight >= 2

    private fun reasonFor(activity: ActivityInstance, recoveryStart: LocalDate): RecoveryReason = when {
        activity.dueAt != null -> RecoveryReason.DEADLINE
        activity.priority.weight == 0 -> RecoveryReason.HIGH_PRIORITY
        activity.priority.weight == 1 -> RecoveryReason.HIGH_PRIORITY
        activity.delayConsequence.weight >= 2 -> RecoveryReason.HIGH_CONSEQUENCE
        activity.source.toString().contains("goal", ignoreCase = true) -> RecoveryReason.LONG_TERM_GOAL
        activity.planned.start.atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(recoveryStart) -> RecoveryReason.LOW_VALUE_WITHOUT_DEADLINE
        else -> RecoveryReason.CAPACITY_AVAILABLE
    }

    private fun deadlineRank(activity: ActivityInstance, recoveryStart: LocalDate): Int = when {
        activity.dueAt == null -> 1
        activity.dueAt!!.atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(recoveryStart) -> 0
        else -> 0
    }

    private fun localDate(activity: ActivityInstance): LocalDate =
        activity.planned.start.atZone(java.time.ZoneOffset.UTC).toLocalDate()
}
