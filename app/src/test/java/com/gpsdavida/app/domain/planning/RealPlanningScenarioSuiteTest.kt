package com.superplanner.app.domain.planning

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.CapacityMode
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.DelayConsequence
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.ScheduleConflictReason
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.usecase.GenerateDailySchedule
import com.superplanner.app.domain.usecase.RescheduleAfterDelay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealPlanningScenarioSuiteTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")
    private val date = LocalDate.of(2026, 9, 13)
    private val engine = DefaultPlanningEngine(
        generateDailySchedule = GenerateDailySchedule(),
        rescheduleAfterDelay = RescheduleAfterDelay(GenerateDailySchedule()),
    )

    @Test
    fun `work study exercise and housework remain schedulable as one realistic day`() {
        val result = plan(listOf(
            activity("work", "09:00", "10:00", Priority.REQUIRED),
            activity("study", "10:00", "11:00", Priority.IMPORTANT),
            activity("exercise", "11:00", "12:00", Priority.IMPORTANT),
            activity("house", "12:00", "13:00", Priority.DESIRABLE),
        ))
        assertEquals(listOf("work", "study", "exercise", "house"), ids(result))
        assertTrue(result.unscheduled.isEmpty())
    }

    @Test
    fun `multiple fixed commitments remain protected when they do not overlap`() {
        val result = plan(listOf(
            activity("meeting", "09:00", "10:00", Priority.REQUIRED, Flexibility.FIXED),
            activity("doctor", "11:00", "12:00", Priority.REQUIRED, Flexibility.FIXED),
            activity("school", "13:00", "14:00", Priority.IMPORTANT, Flexibility.FIXED),
        ))
        assertEquals(listOf("meeting", "doctor", "school"), ids(result))
        assertTrue(result.decisions.all { it.outcome == PlanningDecisionOutcome.SCHEDULED })
    }

    @Test
    fun `routine steps stay in chronological order like a paper checklist`() {
        assertEquals(listOf("wake", "breakfast", "prepare"), ids(plan(listOf(
            activity("wake", "07:00", "07:15"),
            activity("breakfast", "07:15", "07:45"),
            activity("prepare", "07:45", "08:15"),
        ))))
    }

    @Test
    fun `priority conflict preserves the more important activity when the windows overlap`() {
        val result = plan(listOf(
            activity("house", "10:00", "11:00", Priority.DESIRABLE),
            activity("deadline", "10:00", "11:00", Priority.REQUIRED),
        ))
        assertEquals("deadline", ids(result).first())
    }

    @Test
    fun `an impossible pair of fixed commitments is explicitly reported as a scheduling conflict`() {
        val first = activity("first", "09:00", "11:00", Priority.REQUIRED, Flexibility.FIXED)
        val second = activity("second", "10:00", "11:00", Priority.REQUIRED, Flexibility.FIXED)
        val result = GenerateDailySchedule()(listOf(first, second), date, zoneId = ZoneOffset.UTC)
        assertEquals(1, result.conflicts.size)
        assertEquals(ScheduleConflictReason.FIXED_OVERLAP, result.conflicts.single().reason)
        assertEquals("second", result.conflicts.single().activity.id.value)
    }

    @Test
    fun `official route never schedules beyond declared daily capacity`() {
        val result = engine(PlanningInput(
            activities = listOf(
                activity("first", "09:00", "10:00", Priority.REQUIRED),
                activity("second", "10:00", "11:00", Priority.IMPORTANT),
                activity("third", "11:00", "12:00", Priority.DESIRABLE),
            ),
            context = NextActionContext(
                now = start,
                zoneId = ZoneOffset.UTC,
                dailyCapacity = DailyCapacity(Duration.ofHours(2), utilizationLimit = 1.0),
            ),
            date = date,
        ))

        assertEquals(listOf("first", "second"), ids(result))
        assertEquals(1, result.unscheduled.size)
        assertEquals("third", result.unscheduled.single().activity.id.value)
        assertEquals(listOf(PlanningReason.CAPACITY_EXCEEDED), result.unscheduled.single().reasons)
    }

    @Test
    fun `learned duration changes the official planned slot when enough evidence exists`() {
        val result = engine(PlanningInput(
            activities = listOf(activity("study", "09:00", "09:30", Priority.IMPORTANT)),
            context = NextActionContext(
                now = start,
                zoneId = ZoneOffset.UTC,
                learnedDurations = mapOf(ActivityInstanceId("study") to Duration.ofHours(1)),
                dailyCapacity = DailyCapacity(Duration.ofHours(1), utilizationLimit = 1.0),
            ),
            date = date,
        ))

        val study = result.route.single().activity
        assertEquals(Duration.ofHours(1), study.plannedDuration)
        assertTrue(study.planned.end == start.plus(Duration.ofHours(1)))
    }

    @Test
    fun `learned duration can make a previously fitting route conflict with capacity`() {
        val result = engine(PlanningInput(
            activities = listOf(activity("study", "09:00", "09:30", Priority.IMPORTANT)),
            context = NextActionContext(
                now = start,
                zoneId = ZoneOffset.UTC,
                learnedDurations = mapOf(ActivityInstanceId("study") to Duration.ofHours(1)),
                dailyCapacity = DailyCapacity(Duration.ofMinutes(45), utilizationLimit = 1.0),
            ),
            date = date,
        ))

        assertTrue(result.route.isEmpty())
        assertEquals(PlanningReason.CAPACITY_EXCEEDED, result.unscheduled.single().reasons.single())
    }

    @Test
    fun `completed work is not scheduled again when a day is resumed`() {
        val result = plan(listOf(
            activity("done", "09:00", "10:00").copy(status = ActivityStatus.DONE),
            activity("pending", "10:00", "11:00"),
            activity("later", "11:00", "12:00"),
        ))
        assertFalse(ids(result).contains("done"))
        assertEquals(listOf("pending", "later"), ids(result))
    }

    @Test
    fun `a last minute addition is represented by the new route without changing the existing input`() {
        val original = listOf(activity("work", "09:00", "10:00"))
        val after = plan(original + activity("urgent", "10:00", "11:00", Priority.REQUIRED))
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
        val result = engine(PlanningInput(
            activities = listOf(delayed, activity("next", "10:00", "11:00")),
            context = NextActionContext(now = start, zoneId = ZoneOffset.UTC),
            date = date,
            delayedActivity = delayed,
        ))
        assertTrue(result.route.isNotEmpty() || result.unscheduled.isNotEmpty())
        assertTrue(result.decisions.all { it.reasons.isNotEmpty() })
    }

    @Test
    fun `tradeoff scenario never pretends that six hours fit into five`() {
        val result = CapacityTradeoffNegotiator().analyze(
            activities = listOf(
                activity("study", "09:00", "11:00", Priority.IMPORTANT),
                activity("exercise", "11:00", "13:00", Priority.IMPORTANT),
                activity("house", "13:00", "15:00", Priority.DESIRABLE),
            ),
            capacity = DailyCapacity(Duration.ofHours(5), utilizationLimit = 1.0),
        )
        assertTrue(result.overloaded)
        assertEquals(Duration.ofHours(1), result.overload)
        assertTrue(result.requiresUserDecision)
    }

    @Test
    fun `low capacity is a legitimate planning state rather than a productivity failure`() {
        val low = LowCapacityPlanner.capacity(DailyCapacity(normal = Duration.ofHours(8), utilizationLimit = 0.8))
        assertEquals(Duration.ofHours(8), low.normal)
        assertEquals(Duration.ofHours(4), low.declared)
        assertEquals(CapacityMode.EXCEPTIONAL, low.mode)
    }

    @Test
    fun `leisure can remain on the route when it is the explicit activity for the day`() {
        val result = plan(listOf(
            activity("rest", "18:00", "19:00", Priority.LEISURE, Flexibility.FIXED),
            activity("study", "09:00", "10:00", Priority.IMPORTANT),
        ))
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

    private fun plan(activities: List<ActivityInstance>): PlanningResult = engine(PlanningInput(
        activities = activities,
        context = NextActionContext(now = start, zoneId = ZoneOffset.UTC),
        date = date,
    ))

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
