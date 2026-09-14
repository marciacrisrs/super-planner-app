package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.NaturalLanguageActivityParser
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.port.TaskRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalLanguageToScheduleFixedStartTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val today = LocalDate.of(2026, 9, 13)
    private val targetDate = today.plusDays(1)
    private val now = Instant.parse("2026-09-13T12:00:00Z")

    @Test
    fun `natural language fixed time survives creation materialization and scheduling`() = runBlocking {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskFromNaturalLanguageDraft(repository)
        val materialize = MaterializeDailyActivities()
        val scheduler = GenerateDailySchedule()

        val draft = NaturalLanguageActivityParser.parse(
            "Quero estudar francês amanhã às 18h por uma hora",
            today,
        )

        val created = createTask(
            draft = draft,
            confirmed = true,
            now = now,
            zoneId = zone,
        ) as CreateTaskFromNaturalLanguageResult.Created

        val expectedStart = targetDate.atTime(18, 0).atZone(zone).toInstant()
        assertEquals(expectedStart, created.task.fixedStartAt)
        assertEquals(Duration.ofHours(1), created.task.plannedDuration)

        val flexibleTask = Task(
            id = TaskId("flexible-before-fixed"),
            title = "Ler",
            plannedDuration = Duration.ofHours(1),
            priority = Priority.IMPORTANT,
            due = targetDate.atTime(17, 0).atZone(zone).toInstant(),
        )

        val dailyActivities = materialize(
            events = emptyList(),
            tasks = listOf(created.task, flexibleTask),
            habits = emptyList(),
            routines = emptyList(),
            date = targetDate,
            zoneId = zone,
        )

        val fixedMaterialized = dailyActivities.first {
            it.instance.source == ActivitySource.FromTask(created.task.id)
        }.instance
        assertEquals(expectedStart, fixedMaterialized.planned.start)
        assertEquals(Flexibility.FIXED, fixedMaterialized.flexibility)

        val schedule = scheduler(
            activities = dailyActivities.map { it.instance },
            date = targetDate,
            zoneId = zone,
        )

        assertTrue(schedule.conflicts.isEmpty())
        val scheduledFixed = schedule.activities.first { it.id == fixedMaterialized.id }
        val scheduledFlexible = schedule.activities.first { it.id != fixedMaterialized.id }
        assertEquals(expectedStart, scheduledFixed.planned.start)
        assertEquals(expectedStart.plus(Duration.ofHours(1)), scheduledFixed.planned.end)
        assertEquals(targetDate.atTime(17, 0).atZone(zone).toInstant(), scheduledFlexible.planned.start)
        assertEquals(Flexibility.FIXED, scheduledFixed.flexibility)
    }

    private class InMemoryTaskRepository : TaskRepository {
        private val items = linkedMapOf<TaskId, Task>()

        override fun observeAll(): Flow<List<Task>> = flowOf(items.values.toList())

        override suspend fun getById(id: TaskId): Task? = items[id]

        override suspend fun save(task: Task) {
            items[task.id] = task
        }

        override suspend fun delete(id: TaskId) {
            items.remove(id)
        }
    }
}
