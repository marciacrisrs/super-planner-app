package com.superplanner.app.data

import com.superplanner.app.domain.ai.AiCommand
import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Safe placeholder gateway. Real tool adapters are introduced independently and
 * must delegate to existing use cases instead of writing state directly.
 */
@Singleton
class DefaultAiToolGateway @Inject constructor() : AiToolGateway {
    override suspend fun execute(command: AiCommand): AiToolResult = when (command) {
        is AiCommand.ExplainNextActivity -> AiToolResult.Success(
            listOf("explanation_requested", command.activityId),
        )
        is AiCommand.CreateActivityDraft -> AiToolResult.Success(
            listOf("activity_draft", command.title),
        )
        is AiCommand.ReorganizeDay -> AiToolResult.Success(
            listOf("reorganize_requested", command.instruction),
        )
        AiCommand.RecalculateRoute -> AiToolResult.Success(listOf("route_recalculation_requested"))
    }
}