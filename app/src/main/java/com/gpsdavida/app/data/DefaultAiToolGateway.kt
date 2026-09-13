package com.superplanner.app.data

import com.superplanner.app.domain.ai.AiCommand
import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import com.superplanner.app.domain.usecase.CreateTaskFromNaturalLanguageDraft
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Safe gateway: AI commands are translated into existing domain use cases. */
@Singleton
class DefaultAiToolGateway @Inject constructor(
    private val createTaskFromDraft: CreateTaskFromNaturalLanguageDraft,
) : AiToolGateway {
    override suspend fun execute(command: AiCommand): AiToolResult = when (command) {
        is AiCommand.ExplainNextActivity -> AiToolResult.Success(
            buildList {
                add("explanation_requested")
                add(command.activityId)
                command.evidence.forEach { add("evidence=$it") }
            },
        )
        is AiCommand.CreateActivityDraft -> executeCreate(command)
        is AiCommand.ReorganizeDay -> AiToolResult.Success(
            listOf("reorganize_requested", command.request.operation.javaClass.simpleName),
        )
        is AiCommand.MissingInformation -> AiToolResult.Success(
            command.fields.map { "missing=$it" },
        )
        AiCommand.RecalculateRoute -> AiToolResult.Success(listOf("route_recalculation_requested"))
    }

    private suspend fun executeCreate(command: AiCommand.CreateActivityDraft): AiToolResult =
        when (val result = createTaskFromDraft(
            draft = command.draft,
            confirmed = true,
            now = Instant.now(),
        )) {
            is com.superplanner.app.domain.usecase.CreateTaskFromNaturalLanguageResult.Created -> AiToolResult.Success(
                listOf(
                    "activity_created",
                    "id=${result.task.id.value}",
                    "title=${result.task.title}",
                ),
            )
            is com.superplanner.app.domain.usecase.CreateTaskFromNaturalLanguageResult.NeedsConfirmation ->
                AiToolResult.Rejected(result.reason)
            is com.superplanner.app.domain.usecase.CreateTaskFromNaturalLanguageResult.NeedsMoreInformation ->
                AiToolResult.Rejected("missing=${result.fields.joinToString(",")}")
        }
}
