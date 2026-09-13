package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.Dependency
import com.superplanner.app.domain.model.DependencyId
import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.ExecutionContext
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.model.Location
import com.superplanner.app.domain.model.LocationId
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.model.TravelTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class ChooseNextActivityTest {
    private val useCase = ChooseNextActivity()
    private val now = Instant.parse("2026-08-17T10:00:00Z")
    private val zone = ZoneOffset.UTC

    @Test
    fun `current activity is recommended before overdue activity`() {
        val overdue = activity("overdue", "08:00:00", "08:30:00", Priority.REQUIRED)
        val current = activity("current", "09:30:00", "10:30:00", Priority.IMPORTANT)

        val decision = useCase(listOf(overdue, current), NextActionContext(now = now, zoneId = zone))

        assertEquals(current.id, decision.current?.id)
        assertEquals(overdue.id, decision.next?.id)
        assertEquals(current.id, decision.recommended?.id)
    }

    @Test
    fun `priority breaks ties between overdue activities`() {
        val important = activity("important", "09:00:00", "09:30:00", Priority.IMPORTANT)
        val required = activity("required", "09:30:00", "09:45:00", Priority.REQUIRED)

        val decision = useCase(listOf(important, required), NextActionContext(now = now, zoneId = zone))

        assertEquals(required.id, decision.recommended?.id)
    }

    @Test
    fun `blocked availability makes an activity non executable`() {
        val blocked = activity("blocked", "10:30:00", "11:00:00", Priority.REQUIRED)
        val free = activity("free", "10:00:00", "10:30:00", Priority.IMPORTANT)
        val availability = listOf(
            Availability(
                id = AvailabilityId("a1"),
                dayOfWeek = DayOfWeek.MONDAY,
                window = LocalTimeWindow(LocalTime.of(10, 0), LocalTime.of(10, 30)),
                kind = AvailabilityKind.FREE,
            ),
            Availability(
                id = AvailabilityId("a2"),
                dayOfWeek = DayOfWeek.MONDAY,
                window = LocalTimeWindow(LocalTime.of(10, 30), LocalTime.of(11, 0)),
                kind = AvailabilityKind.BLOCKED,
            ),
        )

        val decision = useCase(
            listOf(blocked, free),
            NextActionContext(now = now, zoneId = zone, availability = availability),
        )

        assertEquals(free.id, decision.recommended?.id)
        assertEquals(free.id, decision.current?.id)
    }

    @Test
    fun `dependency blocks successor until predecessor is done`() {
        val predecessor = activity("predecessor", "08:00:00", "08:30:00", Priority.REQUIRED)
        val successor = activity("successor", "10:30:00", "11:00:00", Priority.REQUIRED)
        val dependency = Dependency(
            id = DependencyId("d1"),
            predecessor = predecessor.source,
            successor = successor.source,
        )

        val blockedDecision = useCase(
            listOf(predecessor, successor),
            NextActionContext(now = now, zoneId = zone, dependencies = listOf(dependency)),
        )
        assertEquals(predecessor.id, blockedDecision.recommended?.id)

        val completed = predecessor.copy(status = ActivityStatus.DONE)
        val unblockedDecision = useCase(
            listOf(completed, successor),
            NextActionContext(now = now, zoneId = zone, dependencies = listOf(dependency)),
        )
        assertEquals(successor.id, unblockedDecision.recommended?.id)
    }

    @Test
    fun `completed skipped and deferred activities are ignored`() {
        val done = activity("done", "09:00:00", "09:30:00", Priority.REQUIRED)
            .copy(status = ActivityStatus.DONE)
        val skipped = activity("skipped", "09:30:00", "10:00:00", Priority.REQUIRED)
            .copy(status = ActivityStatus.SKIPPED)
        val deferred = activity("deferred", "10:00:00", "10:30:00", Priority.REQUIRED)
            .copy(status = ActivityStatus.DEFERRED)
        val pending = activity("pending", "11:00:00", "11:30:00", Priority.IMPORTANT)

        val decision = useCase(listOf(done, skipped, deferred, pending), NextActionContext(now = now, zoneId = zone))

        assertEquals(pending.id, decision.recommended?.id)
    }

    @Test
    fun `low current energy favors lower energy activity`() {
        val high = activity("high", "11:00:00", "11:30:00", Priority.IMPORTANT, Energy.HIGH)
        val low = activity("low", "11:30:00", "12:00:00", Priority.IMPORTANT, Energy.LOW)

        val decision = useCase(
            listOf(high, low),
            NextActionContext(now = now, zoneId = zone, currentEnergy = Energy.LOW),
        )

        assertEquals(low.id, decision.recommended?.id)
    }

    @Test
    fun `energy does not override mandatory priority`() {
        val high = activity("high", "11:00:00", "11:30:00", Priority.REQUIRED, Energy.HIGH)
        val low = activity("low", "11:30:00", "12:00:00", Priority.IMPORTANT, Energy.LOW)

        val decision = useCase(
            listOf(high, low),
            NextActionContext(now = now, zoneId = zone, currentEnergy = Energy.LOW),
        )

        assertEquals(high.id, decision.recommended?.id)
    }

    @Test
    fun `without current energy recommendation order remains unchanged`() {
        val high = activity("high", "11:00:00", "11:30:00", Priority.IMPORTANT, Energy.HIGH)
        val low = activity("low", "11:30:00", "12:00:00", Priority.IMPORTANT, Energy.LOW)

        val decision = useCase(listOf(high, low), NextActionContext(now = now, zoneId = zone))

        assertEquals(high.id, decision.recommended?.id)
    }

    @Test
    fun `current context excludes activities requiring another context`() {
        val computer = activity(
            id = "computer",
            start = "11:00:00",
            end = "11:30:00",
            priority = Priority.IMPORTANT,
            contexts = setOf(ExecutionContext.COMPUTER),
        )
        val home = activity(
            id = "home",
            start = "11:30:00",
            end = "12:00:00",
            priority = Priority.IMPORTANT,
            contexts = setOf(ExecutionContext.HOME),
        )

        val decision = useCase(
            listOf(computer, home),
            NextActionContext(now = now, zoneId = zone, currentContext = ExecutionContext.HOME),
        )

        assertEquals(home.id, decision.recommended?.id)
    }

    @Test
    fun `activities without context remain executable in any current context`() {
        val unrestricted = activity("unrestricted", "11:00:00", "11:30:00", Priority.IMPORTANT)

        val decision = useCase(
            listOf(unrestricted),
            NextActionContext(now = now, zoneId = zone, currentContext = ExecutionContext.OUTSIDE),
        )

        assertEquals(unrestricted.id, decision.recommended?.id)
    }

    @Test
    fun `without current context all activities remain eligible`() {
        val computer = activity(
            id = "computer",
            start = "11:00:00",
            end = "11:30:00",
            priority = Priority.IMPORTANT,
            contexts = setOf(ExecutionContext.COMPUTER),
        )

        val decision = useCase(listOf(computer), NextActionContext(now = now, zoneId = zone))

        assertEquals(computer.id, decision.recommended?.id)
    }

    @Test
    fun `travel duration is exposed and next activity remains executable when there is enough time`() {
        val current = activity(
            id = "current",
            start = "09:30:00",
            end = "10:30:00",
            priority = Priority.IMPORTANT,
            location = Location(LocationId("home"), "Casa"),
        )
        val next = activity(
            id = "next",
            start = "10:45:00",
            end = "11:15:00",
            priority = Priority.IMPORTANT,
            location = Location(LocationId("office"), "Trabalho"),
        )

        val decision = useCase(
            listOf(current, next),
            NextActionContext(
                now = now,
                zoneId = zone,
                travelTimes = listOf(
                    TravelTime(LocationId("home"), LocationId("office"), Duration.ofMinutes(15)),
                ),
            ),
        )

        assertEquals(next.id, decision.next?.id)
        assertEquals(Duration.ofMinutes(15), decision.travelDurationToNext)
    }

    @Test
    fun `travel can make a future activity non executable`() {
        val current = activity(
            id = "current",
            start = "09:30:00",
            end = "10:30:00",
            priority = Priority.IMPORTANT,
            location = Location(LocationId("home"), "Casa"),
        )
        val next = activity(
            id = "next",
            start = "10:40:00",
            end = "11:10:00",
            priority = Priority.REQUIRED,
            location = Location(LocationId("office"), "Trabalho"),
        )

        val decision = useCase(
            listOf(current, next),
            NextActionContext(
                now = now,
                zoneId = zone,
                travelTimes = listOf(
                    TravelTime(LocationId("home"), LocationId("office"), Duration.ofMinutes(15)),
                ),
            ),
        )

        assertEquals(current.id, decision.recommended?.id)
        assertEquals(null, decision.next)
    }

    @Test
    fun `current location is used when there is no current activity`() {
        val next = activity(
            id = "next",
            start = "10:30:00",
            end = "11:00:00",
            priority = Priority.IMPORTANT,
            location = Location(LocationId("office"), "Trabalho"),
        )

        val decision = useCase(
            listOf(next),
            NextActionContext(
                now = now,
                zoneId = zone,
                currentLocation = LocationId("home"),
                travelTimes = listOf(
                    TravelTime(LocationId("home"), LocationId("office"), Duration.ofMinutes(30)),
                ),
            ),
        )

        assertEquals(next.id, decision.recommended?.id)
        assertEquals(Duration.ofMinutes(30), decision.travelDurationToNext)
    }

    @Test
    fun `without location travel does not change previous behavior`() {
        val next = activity("next", "10:30:00", "11:00:00", Priority.IMPORTANT)

        val decision = useCase(
            listOf(next),
            NextActionContext(
                now = now,
                zoneId = zone,
                currentLocation = LocationId("home"),
                travelTimes = listOf(
                    TravelTime(LocationId("home"), LocationId("office"), Duration.ofHours(1)),
                ),
            ),
        )

        assertEquals(next.id, decision.recommended?.id)
        assertEquals(Duration.ZERO, decision.travelDurationToNext)
    }

    @Test
    fun `default buffer makes a future activity non executable`() {
        val current = activity("current", "09:30:00", "10:30:00", Priority.IMPORTANT)
        val next = activity("next", "10:40:00", "11:10:00", Priority.REQUIRED)

        val decision = useCase(
            listOf(current, next),
            NextActionContext(now = now, zoneId = zone, defaultBuffer = Duration.ofMinutes(15)),
        )

        assertEquals(null, decision.next)
    }

    @Test
    fun `per activity buffer overrides default buffer`() {
        val current = activity(
            "current",
            "09:30:00",
            "10:30:00",
            Priority.IMPORTANT,
            bufferAfter = Duration.ofMinutes(5),
        )
        val next = activity("next", "10:40:00", "11:10:00", Priority.REQUIRED)

        val decision = useCase(
            listOf(current, next),
            NextActionContext(now = now, zoneId = zone, defaultBuffer = Duration.ofMinutes(15)),
        )

        assertEquals(next.id, decision.next?.id)
    }

    @Test
    fun `buffer combines with travel time`() {
        val current = activity(
            id = "current",
            start = "09:30:00",
            end = "10:30:00",
            priority = Priority.IMPORTANT,
            location = Location(LocationId("home"), "Casa"),
            bufferAfter = Duration.ofMinutes(10),
        )
        val next = activity(
            id = "next",
            start = "10:45:00",
            end = "11:15:00",
            priority = Priority.REQUIRED,
            location = Location(LocationId("office"), "Trabalho"),
        )

        val decision = useCase(
            listOf(current, next),
            NextActionContext(
                now = now,
                zoneId = zone,
                travelTimes = listOf(
                    TravelTime(LocationId("home"), LocationId("office"), Duration.ofMinutes(5)),
                ),
            ),
        )

        assertEquals(next.id, decision.next?.id)
        assertEquals(Duration.ofMinutes(5), decision.travelDurationToNext)
    }

    private fun activity(
        id: String,
        start: String,
        end: String,
        priority: Priority,
        energy: Energy? = null,
        contexts: Set<ExecutionContext> = emptySet(),
        location: Location? = null,
        bufferAfter: Duration? = null,
    ): ActivityInstance {
        val startInstant = Instant.parse("2026-08-17T${start}Z")
        val endInstant = Instant.parse("2026-08-17T${end}Z")
        return ActivityInstance(
            id = ActivityInstanceId(id),
            source = ActivitySource.FromTask(TaskId(id)),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(startInstant, endInstant),
            priority = priority,
            energy = energy,
            contexts = contexts,
            location = location,
            bufferAfter = bufferAfter,
        )
    }
}
