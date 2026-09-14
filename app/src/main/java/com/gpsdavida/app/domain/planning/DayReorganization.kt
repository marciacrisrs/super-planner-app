package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant

/** Structured operation requested by the assistant to adapt the current day. */
sealed interface DayReorganizationOperation {
    data class DelayActivity(
        val activityId: ActivityInstanceId,
        val minutes: Long,
    ) : DayReorganizationOperation

    data class SetCapacity(
        val capacity: DailyCapacity,
    ) : DayReorganizationOperation
}

data class DayReorganizationRequest(
    val operation: DayReorganizationOperation,
    val now: Instant,
)

data class DayReorganizationResult(
    val operation: DayReorganizationOperation,
    val recalculation: RouteRecalculationResult,
    val summary: ReorganizationSummary,
)

data class ReorganizationSummary(
    val changedActivityIds: List<ActivityInstanceId>,
    val movedCount: Int,
    val removedCount: Int,
    val unscheduledCount: Int,
)

/** Applies explicit typed changes, then delegates route decisions to the engine. */
class ReorganizeDay(
    private val recalculator: AdaptiveRouteRecalculator,
) {
    fun execute(
        input: PlanningInput,
        request: DayReorganizationRequest,
    ): DayReorganizationResult {
        val changedInput = when (val operation = request.operation) {
            is DayReorganizationOperation.DelayActivity -> delay(input, operation)
            is DayReorganizationOperation.SetCapacity -> input.copy(
                context = input.context.copy(dailyCapacity = operation.capacity),
            )
        }
        val recalculation = recalculator.recalculate(
            changedInput,
            RecalculationReason.USER_REQUESTED,
        )
        return DayReorganizationResult(
            operation = request.operation,
            recalculation = recalculation,
            summary = ReorganizationSummary(
                changedActivityIds = recalculation.changes.map { it.activityId }.distinct(),
                movedCount = recalculation.changes.count { it.kind == RouteChangeKind.MOVED },
                removedCount = recalculation.changes.count { it.kind == RouteChangeKind.REMOVED },
                unscheduledCount = recalculation.result.unscheduled.size,
            ),
        )
    }

    private fun delay(
        input: PlanningInput,
        operation: DayReorganizationOperation.DelayActivity,
    ): PlanningInput {
        require(operation.minutes > 0) { "Delay must be positive" }
        val activity = input.activities.firstOrNull { it.id == operation.activityId }
            ?: error("Unknown activity: ${operation.activityId.value}")
        require(activity.flexibility != Flexibility.FIXED) {
            "Fixed activities require an explicit user-approved change before they can move"
        }
        val shift = Duration.ofMinutes(operation.minutes)
        return input.copy(
            activities = input.activities.map { current ->
                if (current.id != operation.activityId) current
                else current.copy(planned = current.planned.shiftedBy(shift))
            },
        )
    }
}

private fun TimeRange.shiftedBy(offset: Duration): TimeRange =
    TimeRange(start.plus(offset), end.plus(offset))
