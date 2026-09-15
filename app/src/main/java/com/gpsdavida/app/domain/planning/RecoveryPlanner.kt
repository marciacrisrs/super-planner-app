package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Recovers work from missed days without treating every pending item as mandatory backlog.
 * Recovery uses today's newly available capacity first; it does not silently create a
 * chain of future reschedules when that capacity is already consumed by the user's plan.
 */
class RecoveryPlanner @Inject constructor() {
    operator fun invoke(
        activities: List<ActivityInstance>,
        recoveryStart: LocalDate,
        capacities: Map<LocalDate, DailyCapacity>,
        horizonDays: Int = 7,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): RecoveryPlan {
        require(horizonDays > 0) { "Recovery horizon must be positive" }

        val horizon = (0 until horizonDays).map { recoveryStart.plusDays(it.toLong()) }
        val pendingRecovery = activities
            .asSequence()
            .filter { it.status == ActivityStatus.PENDING || it.status == ActivityStatus.DEFERRED }
            .map { RecoveryItem(it, localDate(it, zoneId)) }
            .filter { it.originalDate.isBefore(recoveryStart) }
            .distinctBy { it.activity.id }
            .toList()

        val future = activities.filter {
            it.status == ActivityStatus.PENDING && !localDate(it, zoneId).isBefore(recoveryStart)
        }

        val committed = horizon.associateWith { date ->
            future.filter { localDate(it, zoneId) == date }
                .fold(Duration.ZERO) { total, activity -> total.plus(activity.plannedDuration) }
        }.toMutableMap()

        val capacityByDay = linkedMapOf<LocalDate, RecoveryCapacity>()
        val assignments = mutableListOf<RecoveryAssignment>()
        val reassess = mutableListOf<RecoveryItem>()
        val recoveryCapacity = capacities[recoveryStart]

        val ordered = pendingRecovery.sortedWith(
            compareBy<RecoveryItem> { deadlineRank(it.activity, recoveryStart, zoneId) }
                .thenBy { it.activity.priority.weight }
                .thenByDescending { it.activity.delayConsequence.weight }
                .thenBy { it.activity.planned.start },
        )

        for (item in ordered) {
            val reason = reasonFor(item.activity, recoveryStart, zoneId)
            val dueDate = item.activity.dueAt?.atZone(zoneId)?.toLocalDate()
            val fitsRecoveryStart = recoveryCapacity != null &&
                (dueDate == null || !dueDate.isBefore(recoveryStart)) &&
                recoveryCapacity.remaining(committed.getValue(recoveryStart)) >= item.activity.plannedDuration

            if (fitsRecoveryStart && shouldRedistribute(item.activity)) {
                committed[recoveryStart] = committed.getValue(recoveryStart).plus(item.activity.plannedDuration)
                assignments += RecoveryAssignment(item, recoveryStart, RecoveryAction.REDISTRIBUTE, reason)
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

    private fun reasonFor(
        activity: ActivityInstance,
        recoveryStart: LocalDate,
        zoneId: ZoneId,
    ): RecoveryReason = when {
        activity.dueAt != null -> RecoveryReason.DEADLINE
        activity.priority.weight <= 1 -> RecoveryReason.HIGH_PRIORITY
        activity.delayConsequence.weight >= 2 -> RecoveryReason.HIGH_CONSEQUENCE
        localDate(activity, zoneId).isBefore(recoveryStart) -> RecoveryReason.LOW_VALUE_WITHOUT_DEADLINE
        else -> RecoveryReason.CAPACITY_AVAILABLE
    }

    private fun deadlineRank(
        activity: ActivityInstance,
        recoveryStart: LocalDate,
        zoneId: ZoneId,
    ): Int = when {
        activity.dueAt == null -> 1
        activity.dueAt?.atZone(zoneId)?.toLocalDate()?.isBefore(recoveryStart) == true -> 0
        else -> 0
    }

    private fun localDate(activity: ActivityInstance, zoneId: ZoneId): LocalDate =
        activity.planned.start.atZone(zoneId).toLocalDate()
}
