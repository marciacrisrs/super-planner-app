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
            override suspend fun save(task: com.superplanner.app.domain.model.Task) { saves++ }
            override fun observe(): Flow<List<com.superplanner.app.domain.model.Task>> = flowOf(emptyList())
        }
        val gateway = DefaultAiToolGateway(CreateTaskFromNaturalLanguageDraft(repository))
        val draft = NaturalLanguageActivityDraft(
            title = "estudar francês",
            plannedDuration = java.time.Duration.ofHours(1),
            priority = Priority.IMPORTANT,
            date = LocalDate.of(2026, 9, 15),
            startTime = null,
            missingFields = emptySet(),
        )

        val result = gateway.execute(AiCommand.CreateActivityDraft(draft))

        assertTrue(result is com.superplanner.app.domain.port.AiToolResult.Rejected)
        assertEquals(0, saves)
    }
}
