package com.superplanner.app.data

import com.superplanner.app.domain.ai.AiCommand
import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Safe placeholder gateway. Real tool adapters delegate to existing use cases
 * instead of writing planner state directly.
 */
@Singleton
class DefaultAiToolGateway @Inject constructor() : AiToolGateway {
    override suspend fun execute(command: AiCommand): AiToolResult = when (command) {
        is AiCommand.ExplainNextActivity -> AiToolResult.Success(
            listOf("explanation_requested", command.activityId),
        )
        is AiCommand.CreateActivityDraft -> AiToolResult.Success(
            buildList {
                add("activity_draft")
                add(command.draft.title)
                command.draft.plannedDuration?.let { add("duration=${it.toMinutes()}m") }
                command.draft.date?.let { add("date=$it") }
                command.draft.startTime?.let { add("time=$it") }
                command.draft.recurrence?.let { add("recurrence=${it.unit}:${it.interval}") }
                command.draft.missingFields.forEach { add("missing=${it.name}") }
            },
        )
        is AiCommand.ReorganizeDay -> AiToolResult.Success(
            listOf("reorganize_requested", command.instruction),
        )
        AiCommand.RecalculateRoute -> AiToolResult.Success(listOf("route_recalculation_requested"))
    }
}
