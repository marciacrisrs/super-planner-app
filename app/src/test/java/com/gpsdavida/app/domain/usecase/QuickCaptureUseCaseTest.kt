package com.superplanner.app.domain.usecase

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickCaptureUseCaseTest {
    @Test
    fun `capture accepts unorganized text and keeps it intact`() {
        val capture = QuickCaptureUseCase()(
            "Comprar presente para minha mãe",
            LocalDate.of(2026, 9, 13),
        )

        assertEquals("Comprar presente para minha mãe", capture.text)
        assertTrue(capture.requiresOrganization)
        assertTrue(capture.draft.title.contains("presente"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank capture is rejected`() {
        QuickCaptureUseCase()("   ")
    }
}
