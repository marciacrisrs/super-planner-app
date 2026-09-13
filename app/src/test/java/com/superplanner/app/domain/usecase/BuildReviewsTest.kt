package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.DailyReview
import com.superplanner.app.domain.model.ReviewSuggestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildReviewsTest {
    @Test
    fun reviewModels_keep_suggestions_as_decisions_not_productivity_scores() {
        val review = DailyReview(
            date = java.time.LocalDate.of(2026, 9, 13),
            priorityTitle = "Projeto importante",
            plannedCount = 3,
            completedCount = 2,
            changed = listOf("Uma reunião mudou."),
            toReorganize = listOf("Estudar"),
            suggestions = listOf(
                ReviewSuggestion("reorg", "Reorganizar", "Preserve o essencial.", "Reorganizar rota"),
            ),
        )

        assertEquals("Projeto importante", review.priorityTitle)
        assertEquals(1, review.plannedCount - review.completedCount)
        assertTrue(review.suggestions.isNotEmpty())
        assertTrue(review.suggestions.none { it.title.contains("score", ignoreCase = true) })
    }
}
