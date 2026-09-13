package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import java.time.Duration
import java.time.LocalDate

/** Immutable what-if snapshot. It never mutates the official planning state. */
data class PlanningScenario(
    val id: String,
    val name: String,
    val base: PlanningInput,
    val changes: List<ScenarioChange> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Scenario id must not be blank" }
        require(name.isNotBlank()) { "Scenario name must not be blank" }
        require(changes.map { it.activityId }.distinct().size == changes.size) {
            "A scenario cannot change the same activity more than once"
        }
    }

    fun snapshot(): PlanningScenario = copy(
        base = base.snapshot(),
        changes = changes.toList(),
    )

    fun materialize(): PlanningInput {
        val changesById = changes.associateBy { it.activityId }
        val updatedActivities = base.activities
            .filter { it.id.value !in changesById.keys || changesById[it.id.value] !is ScenarioChange.Remove }
            .map { activity ->
                when (val change = changesById[activity.id.value]) {
                    null -> activity
                    is ScenarioChange.Replace -> change.applyTo(activity)
                    is ScenarioChange.Remove -> activity
                    is ScenarioChange.Add -> activity
                }
            }
            .toMutableList()

        changes.filterIsInstance<ScenarioChange.Add>().forEach { updatedActivities += it.activity }

        return base.copy(activities = updatedActivities)
    }
}

sealed interface ScenarioChange {
    val activityId: String

    data class Add(
        val activity: ActivityInstance,
    ) : ScenarioChange {
        override val activityId: String get() = activity.id.value
    }

    data class Replace(
        override val activityId: String,
        val plannedDuration: Duration? = null,
        val plannedStart: java.time.Instant? = null,
        val flexibility: Flexibility? = null,
    ) : ScenarioChange {
        fun applyTo(activity: ActivityInstance): ActivityInstance = activity.copy(
            planned = activity.planned.copy(
                start = plannedStart ?: activity.planned.start,
                end = (plannedStart ?: activity.planned.start).plus(plannedDuration ?: activity.plannedDuration),
            ),
            flexibility = flexibility ?: activity.flexibility,
        )
    }

    data class Remove(
        override val activityId: String,
    ) : ScenarioChange
}

/** Pure result of comparing the official route with a simulated route. */
data class ScenarioComparison(
    val scenario: PlanningScenario,
    val baseline: PlanningResult,
    val simulated: PlanningResult,
    val movedActivities: List<String>,
    val newlyUnscheduled: List<String>,
    val newlyScheduled: List<String>,
    val fixedCommitmentImpact: List<String>,
    val priorityImpact: List<String>,
    val capacityImpact: Duration,
) {
    val hasTradeOff: Boolean
        get() = newlyUnscheduled.isNotEmpty() || movedActivities.isNotEmpty() ||
            fixedCommitmentImpact.isNotEmpty() || priorityImpact.isNotEmpty()
}

/** Human-readable scenario impact categories. */
enum class ScenarioImpact {
    CAPACITY,
    PRIORITY,
    FIXED_COMMITMENT,
    RESCHEDULED,
    UNSCHEDULED,
}

fun ScenarioComparison.impactTypes(): Set<ScenarioImpact> = buildSet {
    if (capacityImpact != Duration.ZERO) add(ScenarioImpact.CAPACITY)
    if (priorityImpact.isNotEmpty()) add(ScenarioImpact.PRIORITY)
    if (fixedCommitmentImpact.isNotEmpty()) add(ScenarioImpact.FIXED_COMMITMENT)
    if (movedActivities.isNotEmpty()) add(ScenarioImpact.RESCHEDULED)
    if (newlyUnscheduled.isNotEmpty()) add(ScenarioImpact.UNSCHEDULED)
}

/** Applies only the scenario snapshot; persistence remains the caller's responsibility. */
fun applyScenario(comparison: ScenarioComparison): PlanningInput =
    comparison.scenario.materialize()
