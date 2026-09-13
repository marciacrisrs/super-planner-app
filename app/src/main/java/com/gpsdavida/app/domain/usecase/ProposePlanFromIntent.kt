package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Goal
import com.superplanner.app.domain.model.IntentActivityProposal
import com.superplanner.app.domain.model.IntentPlanProposal
import com.superplanner.app.domain.model.Milestone
import java.time.LocalDate
import javax.inject.Inject

/** Produces a small, confirmable plan proposal without silently creating tasks. */
class ProposePlanFromIntent @Inject constructor() {
    operator fun invoke(intent: String, today: LocalDate = LocalDate.now()): IntentPlanProposal {
        val normalized = intent.trim()
        require(normalized.isNotBlank()) { "Intent cannot be blank" }

        val targetDate = extractTargetDate(normalized, today)
        val goalId = "goal:${normalized.lowercase().hashCode().toString().replace('-', 'n')}"
        val goal = Goal(goalId.toGoalId(), normalized.removeTargetDatePhrase().trim())
        val milestoneId = "$goalId:milestone-1"
        val milestone = targetDate?.let {
            Milestone(
                id = milestoneId,
                title = "Primeiro marco",
                targetDate = it,
                goalId = goal.id,
            )
        }
        val activity = IntentActivityProposal(
            title = "Definir próximo passo de ${goal.title}",
            targetDate = targetDate,
            milestoneId = milestone?.id,
        )

        return IntentPlanProposal(
            intent = normalized,
            goal = goal,
            milestones = listOfNotNull(milestone),
            nextActivities = listOf(activity),
        )
    }

    private fun extractTargetDate(text: String, today: LocalDate): LocalDate? {
        val month = Regex("(?i)até\\s+(janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)")
            .find(text)?.groupValues?.get(1) ?: return null
        val months = mapOf(
            "janeiro" to 1, "fevereiro" to 2, "março" to 3, "abril" to 4,
            "maio" to 5, "junho" to 6, "julho" to 7, "agosto" to 8,
            "setembro" to 9, "outubro" to 10, "novembro" to 11, "dezembro" to 12,
        )
        val monthValue = months.getValue(month.lowercase())
        val year = if (monthValue < today.monthValue) today.year + 1 else today.year
        return LocalDate.of(year, monthValue, 1).withDayOfMonth(LocalDate.of(year, monthValue, 1).lengthOfMonth())
    }

    private fun String.removeTargetDatePhrase(): String =
        replace(Regex("(?i)\\s*até\\s+(janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)"), "")
            .trim()
}

private fun String.toGoalId() = com.superplanner.app.domain.model.GoalId(this)
