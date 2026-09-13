package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.planning.PlanningResult
import com.superplanner.app.domain.planning.PlanningScenario
import com.superplanner.app.domain.planning.RouteSnapshot
import com.superplanner.app.domain.planning.RouteStep
import com.superplanner.app.domain.planning.ScenarioChange
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SimulatePlanningScenarioTypeTest {
    private val now = Instant.parse("2026-09-13T09:00:00Z")
    private val activity = ActivityInstance(
        id = ActivityInstanceId("activity-1"),
        source = ActivitySource.FromTask(TaskId("task-1")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(now, now.plus(Duration.ofHours(1))),
        priority = Priority.IMPORTANT,
    )

    @Test
    fun `newly scheduled ids are exposed as a list`() {
        val engine = PlanningEngine { input ->
            val routeActivities = input.activities
            PlanningResult(
                route = routeActivities.map { RouteStep(it) },
                unscheduled = emptyList(),
            )
        }
        val useCase = SimulatePlanningScenario(engine)
        val base = com.superplanner.app.domain.planning.PlanningInput(
            activities = listOf(activity),
            context = NextActionContext(now = now),
            date = LocalDate.of(2026, 9, 13),
            previousRoute = RouteSnapshot(listOf(activity.id.value)),
        )

        val comparison = useCase(
            PlanningScenario(
                id = "scenario-1",
                name = "Teste",
                base = base,
                changes = listOf(ScenarioChange.Remove(activity.id.value)),
            ),
        )

        assertEquals(emptyList<String>(), comparison.newlyScheduled)
    }
}
