package com.superplanner.app.data

import com.superplanner.app.domain.ai.AiCommand
import com.superplanner.app.domain.ai.NaturalLanguageActivityDraft
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.port.TaskRepository
import com.superplanner.app.domain.usecase.CreateTaskFromNaturalLanguageDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DefaultAiToolGatewayConfirmationTest {
    @Test
    fun `create command is rejected by gateway without explicit confirmation`() = runTest {
        var saves = 0
        val repository = object : TaskRepository {
            override fun observeAll(): Flow<List<com.superplanner.app.domain.model.Task>> = flowOf(emptyList())
            override suspend fun getById(id: com.superplanner.app.domain.model.TaskId): com.superplanner.app.domain.model.Task? = null
            override suspend fun save(task: com.superplanner.app.domain.model.Task) { saves++ }
            override suspend fun delete(id: com.superplanner.app.domain.model.TaskId) = Unit
        }
        val gateway = DefaultAiToolGateway(CreateTaskFromNaturalLanguageDraft(repository))
        val draft = NaturalLanguageActivityDraft(
            sourceText = "estudar francês amanhã por uma hora",
            title = "estudar francês",
            plannedDuration = java.time.Duration.ofHours(1),
            date = LocalDate.of(2026, 9, 15),
            startTime = null,
            recurrence = null,
            priority = Priority.IMPORTANT,
            energy = null,
            missingFields = emptySet(),
        )

        val result = gateway.execute(AiCommand.CreateActivityDraft(draft))

        assertTrue(result is com.superplanner.app.domain.port.AiToolResult.Rejected)
        assertEquals(0, saves)
    }
}
