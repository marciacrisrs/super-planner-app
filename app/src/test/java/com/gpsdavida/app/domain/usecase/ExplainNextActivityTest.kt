package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplainNextActivityTest {
    @Test
    fun `explanation exposes only structured domain evidence`() {
        val activity = com.gpsdavida.app.test.testutil.TestActivities.flexibleTask(
            id = ActivityInstanceId("study"),
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
