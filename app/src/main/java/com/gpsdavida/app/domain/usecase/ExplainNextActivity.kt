package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import java.time.Duration
import javax.inject.Inject

data class NextActionExplanation(
    val activity: ActivityInstance?,
    val facts: List<ExplanationFact>,
    val hasEnoughEvidence: Boolean,
) {
    val fallbackMessage: String
        get() = "Não tenho evidências suficientes para explicar por que esta é a próxima atividade."
}

data class ExplanationFact(
    val reason: NextActionReason,
    val value: String,
)

/** Turns domain evidence into a stable, auditable explanation payload. */
class ExplainNextActivity @Inject constructor() {
    operator fun invoke(decision: NextActionDecision): NextActionExplanation {
        val activity = decision.recommended ?: return NextActionExplanation(
            activity = null,
            facts = emptyList(),
            hasEnoughEvidence = false,
        )

        val reasons = decision.recommendedReasons
        val facts = reasons.mapNotNull { reason ->
            reason.toFact(activity, decision.travelDurationToNext)
        }

        return NextActionExplanation(
            activity = activity,
            facts = facts,
            hasEnoughEvidence = facts.isNotEmpty(),
        )
    }

    private fun NextActionReason.toFact(
        activity: ActivityInstance,
        travelDuration: Duration,
    ): ExplanationFact? = when (this) {
        NextActionReason.CURRENTLY_ACTIVE -> ExplanationFact(this, "a atividade já está em andamento")
        NextActionReason.DUE_NOW -> ExplanationFact(this, "o horário planejado já chegou")
        NextActionReason.HIGHER_PRIORITY -> ExplanationFact(this, "ela tem prioridade maior entre as opções elegíveis")
        NextActionReason.FIXED_COMMITMENT -> ExplanationFact(this, "é um compromisso fixo")
        NextActionReason.AVAILABLE_IN_WINDOW -> ExplanationFact(this, "está dentro de uma janela disponível")
        NextActionReason.ENERGY_MATCH -> ExplanationFact(this, "o nível de energia é compatível")
        NextActionReason.CONTEXT_MATCH -> ExplanationFact(this, "o contexto atual é compatível")
        NextActionReason.DEPENDENCIES_SATISFIED -> ExplanationFact(this, "as dependências necessárias estão satisfeitas")
        NextActionReason.FLEXIBLE_SLOT -> ExplanationFact(this, "há uma janela flexível adequada para encaixá-la")
        NextActionReason.TRAVEL_FITS -> ExplanationFact(this, "deslocamento e transição cabem antes do início")
        NextActionReason.CAPACITY_AVAILABLE -> ExplanationFact(this, "a capacidade disponível comporta a atividade")
    }
}
