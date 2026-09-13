package com.superplanner.app.domain.ai

import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import java.time.LocalDate
import javax.inject.Inject

/** Provider-independent boundary between conversational AI and Planner truth. */
class AiAssistant @Inject constructor(
    private val provider: AiProvider,
    private val tools: AiToolGateway,
) {
    suspend fun propose(request: AiRequest): AiProposal = provider.interpret(request)

    suspend fun execute(proposal: AiProposal, confirmation: AiConfirmation = AiConfirmation.NotConfirmed): AiExecution {
        if (proposal.requiresConfirmation && confirmation !is AiConfirmation.Confirmed) {
            return AiExecution.AwaitingConfirmation(proposal)
        }
        return AiExecution.Executed(tools.execute(proposal.command))
    }
}

data class AiRequest(
    val message: String,
    val context: AiContext = AiContext(),
)

data class AiContext(
    val nowIso: String? = null,
    val activeActivityId: String? = null,
    val minimalRouteFacts: List<String> = emptyList(),
)

data class AiProposal(
    val command: AiCommand,
    val explanation: String,
    val requiresConfirmation: Boolean,
)

sealed interface AiCommand {
    data class CreateActivityDraft(val draft: NaturalLanguageActivityDraft) : AiCommand
    data class ReorganizeDay(val instruction: String) : AiCommand
    data class ExplainNextActivity(val activityId: String) : AiCommand
    data object RecalculateRoute : AiCommand
}

sealed interface AiConfirmation {
    data object Confirmed : AiConfirmation
    data object NotConfirmed : AiConfirmation
}

sealed interface AiExecution {
    data class AwaitingConfirmation(val proposal: AiProposal) : AiExecution
    data class Executed(val result: AiToolResult) : AiExecution
}

interface AiProvider {
    suspend fun interpret(request: AiRequest): AiProposal
}

class RuleBasedAiProvider @Inject constructor() : AiProvider {
    override suspend fun interpret(request: AiRequest): AiProposal {
        val normalized = request.message.trim()
        return when {
            normalized.contains("por que", ignoreCase = true) || normalized.contains("por quê", ignoreCase = true) -> {
                AiProposal(
                    command = AiCommand.ExplainNextActivity(request.context.activeActivityId.orEmpty()),
                    explanation = "Vou explicar usando apenas os fatos estruturados da rota.",
                    requiresConfirmation = false,
                )
            }
            normalized.contains("reorgan", ignoreCase = true) || normalized.contains("atras", ignoreCase = true) -> {
                AiProposal(
                    command = AiCommand.ReorganizeDay(normalized),
                    explanation = "Entendi um pedido para reorganizar o dia; a mudança será executada por um use case conhecido.",
                    requiresConfirmation = true,
                )
            }
            else -> {
                val today = request.context.nowIso?.take(10)?.let(LocalDate::parse) ?: LocalDate.now()
                val draft = NaturalLanguageActivityParser.parse(normalized, today)
                AiProposal(
                    command = AiCommand.CreateActivityDraft(draft),
                    explanation = "Entendi estes dados estruturados. Nada é persistido antes da sua confirmação.",
                    requiresConfirmation = true,
                )
            }
        }
    }
}
