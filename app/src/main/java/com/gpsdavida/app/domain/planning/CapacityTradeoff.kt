package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.usecase.weight
import java.time.Duration
import java.time.Instant

/**
 * Detects capacity overload and turns it into explicit, user-selectable trade-offs.
 *
 * The negotiator never chooses a personal sacrifice on the user's behalf. It protects
 * fixed anchors and proposes deterministic combinations of flexible activities that
 * would fit within the available capacity.
 */
class CapacityTradeoffNegotiator {
    fun analyze(
        activities: List<ActivityInstance>,
        capacity: DailyCapacity,
    ): CapacityNegotiation {
        val pending = activities.filter { it.status == ActivityStatus.PENDING }
        val protectedItems = pending.filter { it.flexibility == Flexibility.FIXED }
        val movableItems = pending.filter { it.flexibility == Flexibility.FLEXIBLE }

        val total = pending.fold(Duration.ZERO) { acc, activity -> acc.plus(activity.plannedDuration) }
        val schedulable = capacity.schedulable
        val overload = total.minus(schedulable).coerceAtLeastZero()

        if (overload.isZero) {
            return CapacityNegotiation(
                totalEstimated = total,
                schedulableCapacity = schedulable,
                protectedItems = protectedItems,
                movableItems = movableItems,
                alternatives = emptyList(),
            )
        }

        val alternatives = buildAlternatives(movableItems, schedulable.minus(protectedItems.sumDuration()))

        return CapacityNegotiation(
            totalEstimated = total,
            schedulableCapacity = schedulable,
            protectedItems = protectedItems,
            movableItems = movableItems,
            alternatives = alternatives,
        )
    }

    fun applyChoice(
        activities: List<ActivityInstance>,
        negotiation: CapacityNegotiation,
        choice: TradeoffChoice,
    ): List<ActivityInstance> {
        val option = negotiation.alternatives.firstOrNull { it.id == choice.optionId }
            ?: error("Unknown trade-off option: ${choice.optionId}")
        val moved = option.movedItems.toSet()
        return activities.map { activity ->
            if (activity.id in moved && activity.status == ActivityStatus.PENDING) {
                activity.deferred()
            } else {
                activity
            }
        }
    }

    private fun buildAlternatives(
        movableItems: List<ActivityInstance>,
        flexibleBudget: Duration,
    ): List<TradeoffOption> {
        if (flexibleBudget.isNegative) {
            return listOf(
                TradeoffOption(
                    id = "protect-anchors-only",
                    movedItems = movableItems.map { it.id },
                    preservedItems = emptyList(),
                    residualOverload = flexibleBudget.negated(),
                ),
            )
        }

        val ordered = movableItems.sortedWith(
            compareBy<ActivityInstance> { it.priority.weight }
                .thenByDescending { it.delayConsequence.weight }
                .thenBy { it.dueAt ?: Instant.MAX }
                .thenBy { it.id.value },
        )

        val preserveHighestPriority = selectThatFit(ordered, flexibleBudget)
        val preserveTopPriorities = selectThatFit(ordered.take(2), flexibleBudget)

        return listOf(
            optionFor("preserve-highest-priority", preserveHighestPriority, ordered, flexibleBudget),
            optionFor("preserve-top-priorities", preserveTopPriorities, ordered, flexibleBudget),
        ).distinct()
    }

    private fun selectThatFit(
        ordered: List<ActivityInstance>,
        budget: Duration,
    ): List<ActivityInstance> {
        var remaining = budget
        val selected = mutableListOf<ActivityInstance>()
        for (activity in ordered) {
            if (activity.plannedDuration <= remaining) {
                selected += activity
                remaining = remaining.minus(activity.plannedDuration)
            }
        }
        return selected
    }

    private fun optionFor(
        id: String,
        preserved: List<ActivityInstance>,
        allMovable: List<ActivityInstance>,
        flexibleBudget: Duration,
    ): TradeoffOption {
        val preservedIds = preserved.map { it.id }.toSet()
        val moved = allMovable.filterNot { it.id in preservedIds }
        val preservedDuration = preserved.sumDuration()
        val residual = preservedDuration.minus(flexibleBudget).coerceAtLeastZero()
        return TradeoffOption(
            id = id,
            movedItems = moved.map { it.id },
            preservedItems = preserved.map { it.id },
            residualOverload = residual,
        )
    }
}

data class CapacityNegotiation(
    val totalEstimated: Duration,
    val schedulableCapacity: Duration,
    val protectedItems: List<ActivityInstance>,
    val movableItems: List<ActivityInstance>,
    val alternatives: List<TradeoffOption>,
) {
    val overloaded: Boolean
        get() = totalEstimated > schedulableCapacity

    val overload: Duration
        get() = totalEstimated.minus(schedulableCapacity).coerceAtLeastZero()

    val requiresUserDecision: Boolean
        get() = overloaded && movableItems.isNotEmpty()
}

data class TradeoffChoice(
    val optionId: String,
)

data class TradeoffOption(
    val id: String,
    val movedItems: List<ActivityInstanceId>,
    val preservedItems: List<ActivityInstanceId>,
    val residualOverload: Duration,
) {
    init {
        require(movedItems.intersect(preservedItems.toSet()).isEmpty()) {
            "An activity cannot be both moved and preserved"
        }
    }
}

private fun Iterable<ActivityInstance>.sumDuration(): Duration =
    fold(Duration.ZERO) { acc, activity -> acc.plus(activity.plannedDuration) }

private fun Duration.coerceAtLeastZero(): Duration =
    if (isNegative) Duration.ZERO else this
