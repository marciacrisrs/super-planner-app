package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateDailyScheduleFixedStartTest {
    private val materialize = MaterializeDailyActivities()
    private val generate = GenerateDailySchedule()
    private val zoneId = ZoneId.of("America/Sao_Paulo")
    private val date = LocalDate.of(2026, 9, 14)

    @Test
    fun scheduler_never_moves_fixed_start_before_user_declared_time() {
        val fixedStart = date.atTime(18, 0).atZone(zoneId).toInstant()
        val fixedTask = Task(
            id = TaskId("fixed"),
            title = "Compromisso das 18h",
            plannedDuration = Duration.ofHours(1),
            priority = Priority.REQUIRED,
            due = fixedStart,
            fixedStartAt = fixedStart,
        )
        val flexibleTask = Task(
            id = TaskId("flexible"),
            title = "Estudar",
            plannedDuration = Duration.ofHours(1),
            priority = Priority.IMPORTANT,
            due = date.atTime(17, 0).atZone(zoneId).toInstant(),
        )

        val activities = materialize(
            events = emptyList(),
            tasks = listOf(fixedTask, flexibleTask),
            habits = emptyList(),
            routines = emptyList(),
            date = date,
            zoneId = zoneId,
        ).map { it.instance }

        val schedule = generate(
            activities = activities,
            date = date,
            zoneId = zoneId,
        )

        val fixed = schedule.activities.single { it.source == com.superplanner.app.domain.model.ActivitySource.FromTask(TaskId("fixed")) }
        val flexible = schedule.activities.single { it.source == com.superplanner.app.domain.model.ActivitySource.FromTask(TaskId("flexible")) }

        assertEquals(Flexibility.FIXED, fixed.flexibility)
        assertEquals(fixedStart, fixed.planned.start)
        assertTrue(flexible.planned.end <= fixed.planned.start)
    }
}
