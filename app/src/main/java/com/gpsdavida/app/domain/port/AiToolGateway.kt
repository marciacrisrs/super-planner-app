package com.superplanner.app.domain.port

import com.superplanner.app.domain.ai.AiCommand

interface AiToolGateway {
    /**
     * Executes a command only when the caller explicitly confirms the proposal.
     * Defaults to false so new callers cannot accidentally authorize mutations.
     */
    suspend fun execute(command: AiCommand, confirmed: Boolean = false): AiToolResult
}

sealed interface AiToolResult {
    data class Success(val facts: List<String>) : AiToolResult
    data class Rejected(val reason: String) : AiToolResult
}
