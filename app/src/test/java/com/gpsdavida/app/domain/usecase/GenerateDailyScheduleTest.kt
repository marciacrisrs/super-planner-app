package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.Dependency
import com.superplanner.app.domain.model.DependencyId
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.model.Location
import com.superplanner.app.domain.model.LocationId
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.model.TravelTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateDailyScheduleTest {
    private val useCase = GenerateDailySchedule()
    private val date = LocalDate.of(2026, 8, 17)
    private val zone = java.time.ZoneOffset.UTC

    @Test
    fun `fixed activities are preserved and flexible activity fills the gap`() {
        val fixed = activity("meeting", "10:00", "11:00", Flexibility.FIXED, Priority.REQUIRED)
        val flexible = activity("task", "09:00", "09:30", Flexibility.FLEXIBLE, Priority.IMPORTANT)

        val result = useCase(listOf(fixed, flexible), date, zoneId = zone)

        assertEquals(listOf("task", "meeting"), result.activities.map { it.id.value })
        assertEquals(Instant.parse("2026-08-17T09:00:00Z"), result.activities.first().planned.start)
        assertTrue(result.isConflictFree)
    }

    @Test
    fun `higher priority flexible activity gets the available slot first`() {
        val low = activity("low", "09:00", "10:00", Flexibility.FLEXIBLE, Priority.DESIRABLE)
        val high = activity("high", "09:00", "10:00", Flexibility.FLEXIBLE, Priority.IMPORTANT)
        val availability = freeWindow("09:00", "11:00")

        val result = useCase(listOf(low, high), date, availability, zoneId = zone)

        assertEquals("high", result.activities[0].id.value)
        assertEquals("low", result.activities[1].id.value)
    }

    @Test
    fun `blocked availability produces a conflict`() {
        val task = activity("task", "10:00", "11:00", Flexibility.FLEXIBLE, Priority.IMPORTANT)
        val availability = listOf(
            Availability(
                AvailabilityId("free"), DayOfWeek.MONDAY,
                LocalTimeWindow(LocalTime.of(9, 0), LocalTime.of(12, 0)), AvailabilityKind.FREE,
            ),
            Availability(
                AvailabilityId("blocked"), DayOfWeek.MONDAY,
                LocalTimeWindow(LocalTime.of(9, 0), LocalTime.of(12, 0)), AvailabilityKind.BLOCKED,
            ),
        )

        val result = useCase(listOf(task), date, availability, zoneId = zone)

        assertEquals(1, result.conflicts.size)
    }

    @Test
    fun `dependency schedules predecessor before successor`() {
        val predecessor = activity("a", "09:00", "09:30", Flexibility.FLEXIBLE, Priority.IMPORTANT)
        val successor = activity("b", "09:00", "09:30", Flexibility.FLEXIBLE, Priority.REQUIRED)
        val dependency = Dependency(DependencyId("d"), predecessor.source, successor.source)

        val result = useCase(listOf(predecessor, successor), date, dependencies = listOf(dependency), zoneId = zone)

        assertEquals(listOf("a", "b"), result.activities.map { it.id.value })
        assertTrue(result.isConflictFree)
    }

    @Test
    fun `buffer and travel time are respected between fixed and flexible activities`() {
        val fixed = activity(
            "meeting", "10:00", "10:30", Flexibility.FIXED, Priority.REQUIRED,
            location = Location(LocationId("home"), "Casa"),
            bufferAfter = Duration.ofMinutes(10),
        )
        val flexible = activity(
            "task", "10:30", "10:45", Flexibility.FLEXIBLE, Priority.IMPORTANT,
            location = Location(LocationId("office"), "Trabalho"),
        )
        val travel = TravelTime(LocationId("home"), LocationId("office"), Duration.ofMinutes(15))

        val result = useCase(listOf(fixed, flexible), date, travelTimes = listOf(travel), zoneId = zone)

        assertEquals(Instant.parse("2026-08-17T10:55:00Z"), result.activities[1].planned.start)
    }

    @Test
    fun `recurrence is handled as materialized daily occurrence`() {
        val occurrence = activity("habit-2026-08-17", "08:00", "08:20", Flexibility.FLEXIBLE, Priority.IMPORTANT)

        val result = useCase(listOf(occurrence), date, zoneId = zone)

        assertEquals(1, result.activities.size)
        assertEquals("habit-2026-08-17", result.activities.single().id.value)
    }

    private fun freeWindow(start: String, end: String) = listOf(
        Availability(
            AvailabilityId("free"), DayOfWeek.MONDAY,
            LocalTimeWindow(LocalTime.parse(start), LocalTime.parse(end)), AvailabilityKind.FREE,
        ),
    )

    private fun activity(
        id: String,
        start: String,
        end: String,
        flexibility: Flexibility,
        priority: Priority,
        location: Location? = null,
        bufferAfter: Duration? = null,
    ): ActivityInstance {
        val startInstant = Instant.parse("2026-08-17T${start}:00Z")
        val endInstant = Instant.parse("2026-08-17T${end}:00Z")
        return ActivityInstance(
            id = ActivityInstanceId(id),
            source = ActivitySource.FromTask(TaskId(id)),
            flexibility = flexibility,
            planned = TimeRange(startInstant, endInstant),
            priority = priority,
            location = location,
            bufferAfter = bufferAfter,
        )
    }
}
