package com.superplanner.app.domain.usecase

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProposePlanFromIntentTest {
    @Test
    fun `intent becomes goal milestone and only next step`() {
        val proposal = ProposePlanFromIntent()(
            "Quero tirar minha certificação Java até dezembro",
            LocalDate.of(2026, 9, 13),
        )

        assertTrue(proposal.requiresConfirmation)
        assertTrue(proposal.goal.title.contains("certificação Java"))
        assertEquals(LocalDate.of(2026, 12, 31), proposal.milestones.single().targetDate)
        assertEquals(1, proposal.nextActivities.size)
        assertTrue(proposal.nextActivities.single().title.contains("próximo passo"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank intent is rejected`() {
        ProposePlanFromIntent()(" ")
    }
}
