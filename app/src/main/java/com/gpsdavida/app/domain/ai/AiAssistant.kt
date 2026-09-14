package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.planning.DayReorganizationOperation
import com.superplanner.app.domain.planning.DayReorganizationRequest
import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/** Provider-independent boundary between conversational AI and Planner truth. */
class AiAssistant @Inject constructor(
    private val provider: AiProvider,
    private val tools: AiToolGateway,
    private val telemetry: AiTelemetry,
) {
    suspend fun propose(request: AiRequest): AiProposal = provider.interpret(request)

    suspend fun execute(proposal: AiProposal, confirmation: AiConfirmation = AiConfirmation.NotConfirmed): AiExecution {
        val commandType = commandTypeOf(proposal.command)
        if (proposal.requiresConfirmation && confirmation !is AiConfirmation.Confirmed) {
            return AiExecution.AwaitingConfirmation(proposal)
        }
        if (confirmation is AiConfirmation.Confirmed) {
            telemetry.proposalConfirmed(commandType)
        }
        return when (val result = tools.execute(proposal.command)) {
            is AiToolResult.Success -> {
                telemetry.executionSucceeded(commandType)
                AiExecution.Executed(result)
            }
            is AiToolResult.Rejected -> {
                telemetry.executionRejected(commandType)
                AiExecution.Executed(result)
            }
        }
    }

    private fun commandTypeOf(command: AiCommand): String = when (command) {
        is AiCommand.CreateActivityDraft -> "CREATE_ACTIVITY_DRAFT"
        is AiCommand.ExplainNextActivity -> "EXPLAIN_NEXT_ACTIVITY"
        is AiCommand.ReorganizeDay -> "REORGANIZE_DAY"
        is AiCommand.MissingInformation -> "MISSING_INFORMATION"
        AiCommand.RecalculateRoute -> "RECALCULATE_ROUTE"
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
    data class ReorganizeDay(val request: DayReorganizationRequest) : AiCommand
    data class ExplainNextActivity(
        val activityId: String,
        val evidence: List<String>,
    ) : AiCommand
    data class MissingInformation(val fields: List<String>) : AiCommand
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
                val evidence = request.context.minimalRouteFacts
                if (evidence.isEmpty()) {
                    AiProposal(
                        command = AiCommand.MissingInformation(listOf("evidências da decisão atual")),
                        explanation = "Não tenho evidências suficientes para explicar esta escolha.",
                        requiresConfirmation = false,
                    )
                } else {
                    AiProposal(
                        command = AiCommand.ExplainNextActivity(
                            activityId = request.context.activeActivityId.orEmpty(),
                            evidence = evidence,
                        ),
                        explanation = "Vou explicar somente com base nas evidências estruturadas da decisão.",
                        requiresConfirmation = false,
                    )
                }
            }
            normalized.contains("reorgan", ignoreCase = true) || normalized.contains("atras", ignoreCase = true) -> {
                buildReorganizationProposal(normalized, request.context)
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

    private fun buildReorganizationProposal(message: String, context: AiContext): AiProposal {
        val activityId = context.activeActivityId?.takeIf(String::isNotBlank)
        val now = context.nowIso?.let(Instant::parse)
        val minutes = Regex("(?i)(\\d+)\\s*min").find(message)?.groupValues?.get(1)?.toLongOrNull()

        if (activityId == null || now == null || minutes == null || minutes <= 0) {
            return AiProposal(
                command = AiCommand.MissingInformation(
                    buildList {
                        if (activityId == null) add("qual atividade deve ser alterada")
                        if (now == null) add("o horário atual")
                        if (minutes == null || minutes <= 0) add("quantos minutos mudou")
                    },
                ),
                explanation = "Preciso de mais uma informação para reorganizar o dia com segurança.",
                requiresConfirmation = false,
            )
        }

        return AiProposal(
            command = AiCommand.ReorganizeDay(
                DayReorganizationRequest(
                    operation = DayReorganizationOperation.DelayActivity(
                        activityId = ActivityInstanceId(activityId),
                        minutes = minutes,
                    ),
                    now = now,
                ),
            ),
            explanation = "Entendi um atraso de $minutes minutos. Vou propor o recálculo, sem editar a rota diretamente.",
            requiresConfirmation = true,
        )
    }
}
