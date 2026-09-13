package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.planning.PlanningScenario
import com.superplanner.app.domain.planning.ScenarioChange
import com.superplanner.app.domain.planning.ScenarioComparison
import java.time.Duration
import javax.inject.Inject

/** Runs a what-if route without changing the official planning state. */
class SimulatePlanningScenario @Inject constructor(
    private val planningEngine: PlanningEngine,
) {
    operator fun invoke(scenario: PlanningScenario): ScenarioComparison {
        val baseline = planningEngine(scenario.base.snapshot()).snapshot()
        val simulatedInput = scenario.materialize()
        val simulated = planningEngine(simulatedInput).snapshot()
        return compare(scenario, baseline, simulated)
    }

    private fun compare(
        scenario: PlanningScenario,
        baseline: com.superplanner.app.domain.planning.PlanningResult,
        simulated: com.superplanner.app.domain.planning.PlanningResult,
    ): ScenarioComparison {
        val baselineById = baseline.route.associateBy { it.activity.id.value }
        val simulatedById = simulated.route.associateBy { it.activity.id.value }
        val baselineUnscheduled = baseline.unscheduled.map { it.activity.id.value }.toSet()
        val simulatedUnscheduled = simulated.unscheduled.map { it.activity.id.value }.toSet()

        val moved = baselineById.keys.intersect(simulatedById.keys)
            .filter { id ->
                baselineById.getValue(id).start != simulatedById.getValue(id).start ||
                    baselineById.getValue(id).end != simulatedById.getValue(id).end
            }
        val newlyUnscheduled = simulatedUnscheduled - baselineUnscheduled
        val newlyScheduled = baselineUnscheduled - simulatedUnscheduled

        val baselineFixed = baselineById.filterValues { it.activity.flexibility.name == "FIXED" }.keys
        val fixedImpacted = baselineFixed.filter { id ->
            id in simulatedUnscheduled ||
                (id in simulatedById && simulatedById.getValue(id).start != baselineById.getValue(id).start)
        }

        val baselinePriority = baseline.route
            .groupBy { it.activity.priority.weight }
            .values.flatten()
            .map { it.activity.id.value }
            .toSet()
        val simulatedPriority = simulated.route
            .groupBy { it.activity.priority.weight }
            .values.flatten()
            .map { it.activity.id.value }
            .toSet()
        val priorityImpact = (baselinePriority - simulatedPriority).toList()

        val baselineCapacity = baseline.route.sumOf { it.activity.plannedDuration.toMillis() }
        val simulatedCapacity = simulated.route.sumOf { it.activity.plannedDuration.toMillis() }

        return ScenarioComparison(
            scenario = scenario.snapshot(),
            baseline = baseline,
            simulated = simulated,
            movedActivities = moved,
            newlyUnscheduled = newlyUnscheduled.toList(),
            newlyScheduled = newlyScheduled.toList(),
            fixedCommitmentImpact = fixedImpacted,
            priorityImpact = priorityImpact,
            capacityImpact = Duration.ofMillis(simulatedCapacity - baselineCapacity),
        )
    }
}
