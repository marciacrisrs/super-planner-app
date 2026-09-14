package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.OrganizeWeekConflict
import com.superplanner.app.domain.ai.OrganizeWeekExplanation
import com.superplanner.app.domain.ai.OrganizeWeekOpportunity
import com.superplanner.app.domain.ai.OrganizeWeekResponse
import com.superplanner.app.domain.ai.OrganizeWeekSummary
import com.superplanner.app.domain.ai.OrganizeWeekProposedItem
import com.superplanner.app.domain.model.*
import com.superplanner.app.domain.port.WeeklyPlanOverride
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApplyWeekOrganizationTest {
    private val zone = ZoneOffset.UTC
    private val date = LocalDate.of(2026, 9, 14)

    @Test
    fun `apply persists proposed movable schedule`() = runTest {
        val repository = FakeOverrideRepository()
        val activity = activity("task-1", Flexibility.FLEXIBLE, 9, 10)
        ApplyWeekOrganization(repository)(week(activity), response(
            OrganizeWeekProposedItem("task-1", "Task", date.toString(), "11:00", "12:00", "AI", false, "Fits better"),
        ), zone)
        assertEquals("11:00", repository.current.single().start.atZone(zone).toLocalTime().toString())
        assertEquals("12:00", repository.current.single().end.atZone(zone).toLocalTime().toString())
    }

    @Test
    fun `apply rejects moving fixed commitment`() = runTest {
        val repository = FakeOverrideRepository()
        val fixed = activity("event-1", Flexibility.FIXED, 9, 10)
        assertThrows(IllegalArgumentException::class.java) {
            runTest { ApplyWeekOrganization(repository)(week(fixed), response(
                OrganizeWeekProposedItem("event-1", "Event", date.toString(), "10:00", "11:00", "AI", true, "Move"),
            ), zone) }
        }
        assertEquals(emptyList<WeeklyPlanOverride>(), repository.current)
    }

    private fun week(activity: ActivityInstance) = WeeklyPlanning(date, date, listOf(
        WeeklyDaySummary(date, listOf(WeeklyActivity(date, "Task", WeeklyActivityKind.TASK, activity)), activity.plannedDuration, Duration.ZERO, 1, 0),
    ))

    private fun activity(id: String, flexibility: Flexibility, startHour: Int, endHour: Int) = ActivityInstance(
        ActivityInstanceId(id), ActivitySource.FromTask(TaskId(id)), flexibility,
        TimeRange(date.atTime(startHour, 0).toInstant(zone), date.atTime(endHour, 0).toInstant(zone)), Priority.IMPORTANT,
    )

    private fun response(item: OrganizeWeekProposedItem) = OrganizeWeekResponse(
        OrganizeWeekSummary(0, 1, 0, 0, 0, 0, 0), listOf(item), emptyList<OrganizeWeekConflict>(),
        emptyList<OrganizeWeekOpportunity>(), emptyList<OrganizeWeekExplanation>(), "test",
    )

    private class FakeOverrideRepository : WeeklyPlanOverrideRepository {
        private val state = MutableStateFlow<List<WeeklyPlanOverride>>(emptyList())
        val current get() = state.value
        override suspend fun save(overrides: List<WeeklyPlanOverride>) { state.value = overrides }
        override fun observe(): Flow<List<WeeklyPlanOverride>> = state
    }
}
