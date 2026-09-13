package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.NaturalLanguageActivityDraft
import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.port.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CreateTaskFromNaturalLanguageDraftTest {
    @Test
    fun `does not persist before explicit confirmation`() = runTest {
        val repository = InMemoryTaskRepository()
        val useCase = CreateTaskFromNaturalLanguageDraft(repository)
        val draft = NaturalLanguageActivityDraft(
            sourceText = "amanhã estudar francês por uma hora",
            title = "francês",
            plannedDuration = Duration.ofHours(1),
            date = LocalDate.of(2026, 9, 14),
            startTime = null,
            recurrence = null,
            priority = Priority.IMPORTANT,
            energy = Energy.MEDIUM,
        )

        val result = useCase(
            draft = draft,
            confirmed = false,
            now = Instant.parse("2026-09-13T19:00:00Z"),
        )

        assertTrue(result is CreateTaskFromNaturalLanguageResult.NeedsConfirmation)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `persists only after confirmation using structured fields`() = runTest {
        val repository = InMemoryTaskRepository()
        val useCase = CreateTaskFromNaturalLanguageDraft(repository)
        val draft = NaturalLanguageActivityDraft(
            sourceText = "amanhã estudar francês por uma hora às 19h",
            title = "francês",
            plannedDuration = Duration.ofHours(1),
            date = LocalDate.of(2026, 9, 14),
            startTime = java.time.LocalTime.of(19, 0),
            recurrence = null,
            priority = Priority.IMPORTANT,
            energy = Energy.LOW,
        )

        val result = useCase(
            draft = draft,
            confirmed = true,
            now = Instant.parse("2026-09-13T19:00:00Z"),
            zoneId = ZoneId.of("America/Sao_Paulo"),
        )

        val task = (result as CreateTaskFromNaturalLanguageResult.Created).task
        assertEquals("francês", task.title)
        assertEquals(Duration.ofHours(1), task.plannedDuration)
        assertEquals(Energy.LOW, task.energy)
        assertEquals(1, repository.saved.size)
        assertEquals(task, repository.saved.single())
    }

    private class InMemoryTaskRepository : TaskRepository {
        val saved = mutableListOf<Task>()

        override fun observeAll(): Flow<List<Task>> = flowOf(saved.toList())
        override suspend fun getById(id: TaskId): Task? = saved.firstOrNull { it.id == id }
        override suspend fun save(task: Task) {
            saved.removeAll { it.id == task.id }
            saved += task
        }
        override suspend fun delete(id: TaskId) {
            saved.removeAll { it.id == id }
        }
    }
}
