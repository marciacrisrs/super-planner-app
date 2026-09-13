package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.planning.PlanningInput
import com.superplanner.app.domain.planning.PlanningResult
import com.superplanner.app.domain.planning.PlanningScenario
import com.superplanner.app.domain.planning.RouteStep
import com.superplanner.app.domain.planning.ScenarioChange
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatePlanningScenarioTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    @Test
    fun `simulation uses the same engine and leaves the base input untouched`() {
        var calls = 0
        val engine = PlanningEngine { input ->
            calls += 1
            PlanningResult(
                route = input.activities.map { RouteStep(it) },
                unscheduled = emptyList(),
            )
        }
        val activity = activity("study", 60)
        val input = PlanningInput(
            activities = listOf(activity),
            context = NextActionContext(now = start),
        )
        val scenario = PlanningScenario(
            id = "study-longer",
            name = "Estudar por mais tempo",
            base = input,
            changes = listOf(
                ScenarioChange.Replace("study", plannedDuration = Duration.ofMinutes(90)),
            ),
        )

        val result = SimulatePlanningScenario(engine)(scenario)

        assertEquals(2, calls)
        assertEquals(Duration.ofMinutes(60), input.activities.single().plannedDuration)
        assertEquals(Duration.ofMinutes(90), result.simulated.route.single().activity.plannedDuration)
        assertFalse(result.hasTradeOff)
        assertEquals(Duration.ofMinutes(30), result.capacityImpact)
    }

    @Test
    fun `scenario comparison exposes a newly unscheduled activity as a trade-off`() {
        val activity = activity("gym", 60)
        val engine = PlanningEngine { input ->
            val scheduled = input.activities.take(1)
            val unscheduled = input.activities.drop(1)
            PlanningResult(
                route = scheduled.map { RouteStep(it) },
                unscheduled = unscheduled.map { com.superplanner.app.domain.planning.UnscheduledActivity(it, listOf(com.superplanner.app.domain.planning.PlanningReason.CAPACITY_EXCEEDED)) },
            )
        }
        val input = PlanningInput(
            activities = listOf(activity),
            context = NextActionContext(now = start),
        )
        val scenario = PlanningScenario(
            id = "same-day-extra",
            name = "Adicionar outra atividade",
            base = input,
            changes = listOf(ScenarioChange.Add(activity("second", 60))),
        )

        val result = SimulatePlanningScenario(engine)(scenario)

        assertTrue(result.hasTradeOff)
        assertEquals(listOf("second"), result.newlyUnscheduled)
    }

    private fun activity(id: String, minutes: Long) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(start, start.plus(Duration.ofMinutes(minutes))),
    )
}
