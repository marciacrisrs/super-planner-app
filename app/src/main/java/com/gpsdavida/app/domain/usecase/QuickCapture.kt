package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.NaturalLanguageActivityDraft
import com.superplanner.app.domain.ai.NaturalLanguageActivityParser
import java.time.LocalDate
import javax.inject.Inject

data class QuickCapture(
    val text: String,
    val draft: NaturalLanguageActivityDraft,
    val requiresOrganization: Boolean,
)

/** Captures first; organization can happen later. */
class QuickCaptureUseCase @Inject constructor() {
    operator fun invoke(text: String, today: LocalDate = LocalDate.now()): QuickCapture {
        val normalized = text.trim()
        require(normalized.isNotBlank()) { "Capture cannot be blank" }
        return QuickCapture(
            text = normalized,
            draft = NaturalLanguageActivityParser.parse(normalized, today),
            requiresOrganization = true,
        )
    }
}
