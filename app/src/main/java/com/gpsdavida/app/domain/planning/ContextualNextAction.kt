package com.superplanner.app.domain.planning

import java.time.Duration
import java.time.Instant

data class NextActionCandidate(
    val id: String,
    val title: String,
    val duration: Duration,
    val priorityWeight: Int,
    val availableFrom: Instant,
    val deadline: Instant? = null,
    val preparation: Duration = Duration.ZERO,
    val travel: Duration = Duration.ZERO,
    val dependencySatisfied: Boolean = true,
    val fixed: Boolean = false,
) {
    init {
        require(!duration.isNegative && !duration.isZero) { "duration must be positive" }
        require(!preparation.isNegative && !travel.isNegative) { "preparation and travel cannot be negative" }
    }

    val requiredTime: Duration get() = duration.plus(preparation).plus(travel)
}

data class NextActionContext(
    val now: Instant,
    val availableUntil: Instant,
    val capacityRemaining: Duration,
    val upcomingCommitmentAt: Instant? = null,
)

data class ContextualNextAction(
    val recommended: NextActionCandidate?,
    val alternatives: List<NextActionCandidate>,
    val rejected: List<RejectedNextAction>,
)

data class RejectedNextAction(
    val candidateId: String,
    val reason: String,
)

/**
 * Domain-owned feasibility gate for "what do I do now?". AI may rank/explain only candidates
 * that this engine marks feasible.
 */
class ContextualNextActionSelector {
    fun select(
        context: NextActionContext,
        candidates: List<NextActionCandidate>,
    ): ContextualNextAction {
        val rejected = mutableListOf<RejectedNextAction>()
        val feasible = candidates.filter { candidate ->
            val available = minDuration(context.availableUntil.minus(context.now), context.capacityRemaining)
            when {
                candidate.availableFrom.isAfter(context.now) -> {
                    rejected += RejectedNextAction(candidate.id, "atividade ainda não está disponível")
                    false
                }
                !candidate.dependencySatisfied -> {
                    rejected += RejectedNextAction(candidate.id, "dependência ainda não foi concluída")
                    false
                }
                context.upcomingCommitmentAt != null && context.now.plus(candidate.requiredTime).isAfter(context.upcomingCommitmentAt) -> {
                    rejected += RejectedNextAction(candidate.id, "não cabe antes do próximo compromisso")
                    false
                }
                candidate.deadline != null && context.now.plus(candidate.requiredTime).isAfter(candidate.deadline) -> {
                    rejected += RejectedNextAction(candidate.id, "não cabe antes do prazo")
                    false
                }
                candidate.requiredTime > available -> {
                    rejected += RejectedNextAction(candidate.id, "duração + preparação + deslocamento excedem a capacidade disponível")
                    false
                }
                else -> true
            }
        }

        val ranked = feasible.sortedWith(
            compareByDescending<NextActionCandidate> { it.priorityWeight }
                .thenBy { it.requiredTime }
                .thenBy { it.deadline ?: Instant.MAX }
                .thenBy { it.id },
        )

        return ContextualNextAction(
            recommended = ranked.firstOrNull(),
            alternatives = ranked.drop(1).take(2),
            rejected = rejected,
        )
    }

    private fun minDuration(first: Duration, second: Duration): Duration =
        if (first < second) first else second
}
