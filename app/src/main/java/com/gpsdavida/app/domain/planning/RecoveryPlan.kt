package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.DailyCapacity
import java.time.Duration
import java.time.LocalDate

/** Result of recovering pending work after one or more days that did not go as planned. */
data class RecoveryPlan(
    val assignments: List<RecoveryAssignment>,
    val reassess: List<RecoveryItem>,
    val capacityByDay: Map<LocalDate, RecoveryCapacity>,
) {
    val scheduled: List<RecoveryAssignment> get() = assignments.filter { it.action == RecoveryAction.REDISTRIBUTE }
}

data class RecoveryAssignment(
    val item: RecoveryItem,
    val date: LocalDate,
    val action: RecoveryAction,
    val reason: RecoveryReason,
)

data class RecoveryItem(
    val activity: ActivityInstance,
    val originalDate: LocalDate,
)

data class RecoveryCapacity(
    val date: LocalDate,
    val capacity: DailyCapacity,
    val committed: Duration,
    val remaining: Duration,
)

enum class RecoveryAction {
    REDISTRIBUTE,
    REASSESS,
}

enum class RecoveryReason {
    DEADLINE,
    HIGH_PRIORITY,
    HIGH_CONSEQUENCE,
    LONG_TERM_GOAL,
    CAPACITY_AVAILABLE,
    LOW_VALUE_WITHOUT_DEADLINE,
}
