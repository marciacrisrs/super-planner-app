package com.superplanner.app.domain.planning

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrganizeWeekProposalTest {
    private val date = LocalDate.of(2026, 9, 14)

    @Test
    fun proposal_preserves_original_and_apply_keeps_a_recoverable_snapshot() {
        val fixed = item("work", "Trabalho", 9, 18, fixed = true)
        val flexible = item("study", "Estudar", 19, 20, fixed = false)
        val moved = item("study", "Estudar", 20, 21, fixed = false)

        val proposal = WeekOrganizationProposal(
            original = listOf(fixed, flexible),
            proposed = listOf(fixed, moved),
            conflicts = emptyList(),
            opportunities = listOf("janela noturna preservada"),
            explanations = listOf("A atividade foi deslocada sem tocar no compromisso fixo."),
        )

        val applied = proposal.apply()
        assertEquals(listOf(fixed, flexible), applied.original)
        assertEquals(listOf(fixed, moved), applied.applied)
        assertTrue(applied.changes.any { it is WeekPlanItemChange.Changed })
    }

    @Test(expected = IllegalArgumentException::class)
    fun fixed_item_cannot_change_silently() {
        val fixed = item("work", "Trabalho", 9, 18, fixed = true)
        val changed = item("work", "Trabalho", 10, 19, fixed = true)
        WeekOrganizationProposal(
            original = listOf(fixed),
            proposed = listOf(changed),
            conflicts = emptyList(),
            opportunities = emptyList(),
            explanations = emptyList(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun overlapping_proposal_is_rejected_before_apply() {
        WeekOrganizationProposal(
            original = emptyList(),
            proposed = listOf(
                item("a", "A", 9, 11, false),
                item("b", "B", 10, 12, false),
            ),
            conflicts = listOf("overlap"),
            opportunities = emptyList(),
            explanations = emptyList(),
        )
    }

    private fun item(id: String, title: String, start: Int, end: Int, fixed: Boolean) = WeekPlanItemSnapshot(
        id = id,
        title = title,
        date = date,
        startTime = LocalTime.of(start, 0),
        endTime = LocalTime.of(end, 0),
        fixed = fixed,
    )
}
