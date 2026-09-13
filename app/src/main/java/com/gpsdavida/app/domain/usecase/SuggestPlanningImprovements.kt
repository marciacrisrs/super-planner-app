package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.PlanningEvidence
import com.superplanner.app.domain.model.PlanningImprovementSuggestion
import com.superplanner.app.domain.model.PlanningImprovementType
import java.time.Duration
import javax.inject.Inject

/**
 * Finds repeated planning mismatches without changing any plan automatically.
 * A suggestion is emitted only when there is enough evidence to support it.
 */
class SuggestPlanningImprovements @Inject constructor() {
    companion object {
        private const val MIN_OBSERVATIONS = 3
        private const val MIN_DELAYED_OBSERVATIONS = 3
        private const val MIN_DURATION_VARIANCE_PERCENT = 20
    }

    operator fun invoke(executions: List<ActivityExecution>): List<PlanningImprovementSuggestion> =
        executions
            .groupBy { it.activityInstanceId }
            .values
            .flatMap { activityHistory -> suggestionsFor(activityHistory) }

    private fun suggestionsFor(history: List<ActivityExecution>): List<PlanningImprovementSuggestion> {
        if (history.size < MIN_OBSERVATIONS) return emptyList()
        val newest = history.takeLast(5)
        return buildList {
            durationSuggestion(newest)?.let(::add)
            deferralSuggestion(newest)?.let(::add)
        }
    }

    private fun durationSuggestion(history: List<ActivityExecution>): PlanningImprovementSuggestion? {
        val completed = history.mapNotNull { execution ->
            val actual = execution.actual?.duration ?: return@mapNotNull null
            execution to actual
        }
        if (completed.size < MIN_OBSERVATIONS) return null

        val ratios = completed.map { (execution, actual) ->
            if (execution.planned.duration.isZero) 0.0
            else actual.toMinutes().toDouble() / execution.planned.duration.toMinutes().coerceAtLeast(1)
        }
        val consistentlyLonger = ratios.count { it >= 1.0 + MIN_DURATION_VARIANCE_PERCENT / 100.0 }
        if (consistentlyLonger < MIN_OBSERVATIONS) return null

        val averageActualMinutes = completed.map { it.second.toMinutes() }.average().toLong()
        val suggestedDuration = Duration.ofMinutes(averageActualMinutes)
        val evidence = PlanningEvidence(
            activityInstanceId = completed.first().first.activityInstanceId,
            observationCount = completed.size,
            matchingObservationCount = consistentlyLonger,
            observedValues = completed.map { (execution, actual) ->
                "planned=${execution.planned.duration.toMinutes()}m, actual=${actual.toMinutes()}m"
            },
        )
        return PlanningImprovementSuggestion(
            type = PlanningImprovementType.IncreaseTypicalDuration,
            title = "Essa atividade costuma levar mais tempo",
            explanation = "Nos últimos ${completed.size} registros, a duração real foi pelo menos 20% maior em $consistentlyLonger deles.",
            evidence = listOf(evidence),
            sampleSize = completed.size,
            suggestedDuration = suggestedDuration,
        )
    }

    private fun deferralSuggestion(history: List<ActivityExecution>): PlanningImprovementSuggestion? {
        val deferredCount = history.count { it.status == ActivityStatus.DEFERRED }
        if (deferredCount < MIN_DELAYED_OBSERVATIONS) return null

        val evidence = PlanningEvidence(
            activityInstanceId = history.first().activityInstanceId,
            observationCount = history.size,
            matchingObservationCount = deferredCount,
            observedValues = history.filter { it.status == ActivityStatus.DEFERRED }.map { "status=DEFERRED" },
        )
        return PlanningImprovementSuggestion(
            type = PlanningImprovementType.RepeatedDeferral,
            title = "Essa atividade está sendo adiada com frequência",
            explanation = "Ela foi adiada $deferredCount vezes em ${history.size} observações.",
            evidence = listOf(evidence),
            sampleSize = history.size,
        )
    }
}
