package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.DelayConsequence
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.usecase.GenerateDailySchedule
import com.superplanner.app.domain.usecase.RescheduleAfterDelay
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Real-life scenarios for the planning engine.
 *
 * Each test intentionally keeps the same framing:
 * - input: a small slice of a realistic day;
 * - expected route/decision: what a paper planner would preserve or move;
 * - digital value: the rule is exercised automatically and regressions stay local.
 */
class RealPlanningScenarioSuiteTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    private val engine = DefaultPlanningEngine(
        generateDailySchedule = GenerateDailySchedule(),
        rescheduleAfterDelay = RescheduleAfterDelay(GenerateDailySchedule()),
    )

    @Test
    fun `work study exercise and housework remain schedulable as one realistic day`() {
        val activities = listOf(
            activity("work", "09:00", "10:00", Priority.REQUIRED),
            activity("study", "10:00", "11:00", Priority.IMPORTANT),
            activity("exercise", "11:00", "12:00", Priority.IMPORTANT),
            activity("house", "12:00", "13:00", Priority.DESIRABLE),
        )

        val result = plan(activities)

        assertEquals(listOf("work", "study", "exercise", "house"), ids(result))
        assertTrue(result.unscheduled.isEmpty())
    }

    @Test
    fun `multiple fixed commitments remain protected when they do not overlap`() {
        val activities = listOf(
            activity("meeting", "09:00", "10:00", Priority.REQUIRED, Flexibility.FIXED),
            activity("doctor", "11:00", "12:00", Priority.REQUIRED, Flexibility.FIXED),
            activity("school", "13:00", "14:00", Priority.IMPORTANT, Flexibility.FIXED),
        )

        val result = plan(activities)

        assertEquals(listOf("meeting", "doctor", "school"), ids(result))
        assertTrue(result.decisions.all { it.outcome == PlanningDecisionOutcome.SCHEDULED })
    }

    @Test
    fun `routine steps stay in chronological order like a paper checklist`() {
        val activities = listOf(
            activity("wake", "07:00", "07:15"),
            activity("breakfast", "07:15", "07:45"),
            activity("prepare", "07:45", "08:15"),
        )

        assertEquals(listOf("wake", "breakfast", "prepare"), ids(plan(activities)))
    }

    @Test
    fun `priority conflict preserves the more important activity when the windows overlap`() {
        val important = activity("deadline", "10:00", "11:00", Priority.REQUIRED)
        val desirable = activity("house", "10:00", "11:00", Priority.DESIRABLE)

        val result = plan(listOf(desirable, important))

        assertEquals("deadline", ids(result).first())
    }

    @Test
    fun `an impossible pair of fixed commitments becomes an explicit conflict`() {
        val first = activity("first", "09:00", "11:00", Priority.REQUIRED, Flexibility.FIXED)
        val second = activity("second", "10:00", "11:00", Priority.REQUIRED, Flexibility.FIXED)

        val result = plan(listOf(first, second))

        assertEquals(1, result.unscheduled.size)
        assertEquals(PlanningReason.REQUIRED_CONFLICT, result.unscheduled.single().reasons.single())
        assertEquals(PlanningDecisionOutcome.BLOCKED, result.decisions.single { it.activityId == "second" }.outcome)
    }

    @Test
    fun `completed work is not scheduled again when a day is resumed`() {
        val completed = activity("done", "09:00", "10:00").copy(status = ActivityStatus.DONE)
        val pending = activity("pending", "10:00", "11:00")
        val later = activity("later", "11:00", "12:00")

        val result = plan(listOf(completed, pending, later))

        assertFalse(ids(result).contains("done"))
        assertEquals(listOf("pending", "later"), ids(result))
    }

    @Test
    fun `a last minute addition is represented by the new route without changing the existing input`() {
        val original = listOf(activity("work", "09:00", "10:00"))
        val changed = original + activity("urgent", "10:00", "11:00", Priority.REQUIRED)

        val before = plan(original)
        val after = plan(changed)

        assertEquals(listOf("work"), ids(before))
        assertEquals(listOf("work", "urgent"), ids(after))
        assertEquals(listOf("work"), ids(plan(original)))
    }

    @Test
    fun `delayed activity still produces a route instead of disappearing`() {
        val delayed = activity("delayed", "09:00", "10:00").copy(
            actual = TimeRange(start, start.plus(Duration.ofHours(2))),
            status = ActivityStatus.DONE,
            delayConsequence = DelayConsequence.MODERATE,
        )
        val next = activity("next", "10:00", "11:00")

        val result = engine(
            PlanningInput(
                activities = listOf(delayed, next),
                context = NextActionContext(now = start, zoneId = ZoneOffset.UTC),
                delayedActivity = delayed,
            ),
        )

        assertTrue(result.route.isNotEmpty() || result.unscheduled.isNotEmpty())
        assertTrue(result.decisions.all { it.reasons.isNotEmpty() })
    }

    @Test
    fun `tradeoff scenario never pretends that six hours fit into five`() {
        val negotiator = CapacityTradeoffNegotiator()
        val activities = listOf(
            activity("study", "09:00", "11:00", Priority.IMPORTANT),
            activity("exercise", "11:00", "13:00", Priority.IMPORTANT),
            activity("house", "13:00", "15:00", Priority.DESIRABLE),
        )

        val result = negotiator.analyze(
            activities = activities,
            capacity = DailyCapacity(Duration.ofHours(5), utilizationLimit = 1.0),
        )

        assertTrue(result.overloaded)
        assertEquals(Duration.ofHours(1), result.overload)
        assertTrue(result.requiresUserDecision)
    }

    @Test
    fun `low capacity is a legitimate planning state rather than a productivity failure`() {
        val normal = DailyCapacity(normal = Duration.ofHours(8), utilizationLimit = 0.8)

        val low = LowCapacityPlanner.capacity(normal)

        assertEquals(Duration.ofHours(8), low.normal)
        assertEquals(Duration.ofHours(4), low.declared)
        assertEquals(CapacityMode.EXCEPTIONAL, low.mode)
    }

    @Test
    fun `leisure can remain on the route when it is the explicit activity for the day`() {
        val rest = activity("rest", "18:00", "19:00", Priority.LEISURE, Flexibility.FIXED)
        val study = activity("study", "09:00", "10:00", Priority.IMPORTANT)

        val result = plan(listOf(rest, study))

        assertTrue(ids(result).contains("rest"))
        assertTrue(result.unscheduled.isEmpty())
    }

    @Test
    fun `every scenario decision stays explainable at the route boundary`() {
        val activities = listOf(
            activity("priority", "09:00", "10:00", Priority.REQUIRED),
            activity("flexible", "10:00", "11:00", Priority.IMPORTANT),
        )

        val result = plan(activities)

        assertEquals(activities.size, result.decisions.size)
        assertTrue(result.decisions.all { it.reasons.isNotEmpty() })
    }

    private fun plan(activities: List<ActivityInstance>): PlanningResult = engine(
        PlanningInput(
            activities = activities,
            context = NextActionContext(now = start, zoneId = ZoneOffset.UTC),
        ),
    )

    private fun ids(result: PlanningResult): List<String> = result.route.map { it.activity.id.value }

    private fun activity(
        id: String,
        startTime: String,
        endTime: String,
        priority: Priority = Priority.IMPORTANT,
        flexibility: Flexibility = Flexibility.FLEXIBLE,
    ) = ActivityInstance(
        id = ActivityInstanceId(id),
        source = ActivitySource.FromTask(TaskId("task-$id")),
        flexibility = flexibility,
        planned = TimeRange(
            Instant.parse("2026-09-13T$startTime:00Z"),
            Instant.parse("2026-09-13T$endTime:00Z"),
        ),
        priority = priority,
    )
}
