package com.superplanner.app.domain.port

import com.superplanner.app.domain.ai.AiCommand

interface AiToolGateway {
    suspend fun execute(command: AiCommand): AiToolResult
}

sealed interface AiToolResult {
    data class Success(val facts: List<String>) : AiToolResult
    data class Rejected(val reason: String) : AiToolResult
}