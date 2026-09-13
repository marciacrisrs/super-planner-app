package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanningScenarioTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")
    private val baseActivity = ActivityInstance(
        id = ActivityInstanceId("study"),
        source = ActivitySource.FromTask(TaskId("study-task")),
        flexibility = Flexibility.FLEXIBLE,
        planned = TimeRange(start, start.plus(Duration.ofMinutes(60))),
    )

    private val baseInput = PlanningInput(
        activities = listOf(baseActivity),
        context = NextActionContext(now = start),
        date = LocalDate.parse("2026-09-13"),
    )

    @Test
    fun `materializing a scenario does not mutate the base snapshot`() {
        val scenario = PlanningScenario(
            id = "longer-study",
            name = "Estudar 1h30",
            base = baseInput,
            changes = listOf(
                ScenarioChange.Replace(
                    activityId = "study",
                    plannedDuration = Duration.ofMinutes(90),
                ),
            ),
        )

        val simulated = scenario.materialize()

        assertEquals(Duration.ofMinutes(60), baseInput.activities.single().plannedDuration)
        assertEquals(Duration.ofMinutes(90), simulated.activities.single().plannedDuration)
        assertNotSame(baseInput.activities, simulated.activities)
    }

    @Test
    fun `adding a scenario activity affects only the simulated input`() {
        val added = baseActivity.copy(
            id = ActivityInstanceId("gym"),
            source = ActivitySource.FromTask(TaskId("gym-task")),
            planned = TimeRange(start.plus(Duration.ofHours(2)), start.plus(Duration.ofHours(3))),
        )
        val scenario = PlanningScenario(
            id = "keep-gym",
            name = "Manter academia",
            base = baseInput,
            changes = listOf(ScenarioChange.Add(added)),
        )

        val simulated = scenario.materialize()

        assertEquals(1, baseInput.activities.size)
        assertEquals(2, simulated.activities.size)
        assertTrue(simulated.activities.any { it.id == added.id })
    }

    @Test
    fun `apply scenario returns the scenario snapshot rather than changing the original input`() {
        val scenario = PlanningScenario(
            id = "move-study",
            name = "Mover estudo",
            base = baseInput,
            changes = listOf(
                ScenarioChange.Replace(
                    activityId = "study",
                    plannedStart = start.plus(Duration.ofHours(2)),
                ),
            ),
        )
        val comparison = ScenarioComparison(
            scenario = scenario,
            baseline = PlanningResult(emptyList(), emptyList()),
            simulated = PlanningResult(emptyList(), emptyList()),
            movedActivities = listOf("study"),
            newlyUnscheduled = emptyList(),
            newlyScheduled = emptyList(),
            fixedCommitmentImpact = emptyList(),
            priorityImpact = emptyList(),
            capacityImpact = Duration.ZERO,
        )

        val applied = applyScenario(comparison)

        assertEquals(start, baseInput.activities.single().planned.start)
        assertEquals(start.plus(Duration.ofHours(2)), applied.activities.single().planned.start)
    }
}
