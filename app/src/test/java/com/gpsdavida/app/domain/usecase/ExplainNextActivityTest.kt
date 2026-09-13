package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplainNextActivityTest {
    private val start = Instant.parse("2026-09-13T09:00:00Z")

    @Test
    fun `explanation exposes only structured domain evidence`() {
        val activity = ActivityInstance(
            id = ActivityInstanceId("study"),
            source = ActivitySource.FromTask(TaskId("study-task")),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(start, start.plusSeconds(3600)),
        )
        val explanation = ExplainNextActivity()(
            NextActionDecision(
                current = null,
                next = activity,
                nextReasons = listOf(
                    NextActionReason.HIGHER_PRIORITY,
                    NextActionReason.AVAILABLE_IN_WINDOW,
                    NextActionReason.CAPACITY_AVAILABLE,
                ),
            ),
        )

        assertTrue(explanation.hasEnoughEvidence)
        assertEquals(3, explanation.facts.size)
        assertTrue(explanation.facts.all { it.value.isNotBlank() })
    }

    @Test
    fun `no recommendation has no explanation evidence`() {
        val explanation = ExplainNextActivity()(
            NextActionDecision(current = null, next = null),
        )

        assertFalse(explanation.hasEnoughEvidence)
        assertEquals(0, explanation.facts.size)
        assertEquals(
            "Não tenho evidências suficientes para explicar por que esta é a próxima atividade.",
            explanation.fallbackMessage,
        )
    }
}
