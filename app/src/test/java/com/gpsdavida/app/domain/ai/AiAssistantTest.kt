package com.superplanner.app.domain.ai

import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAssistantTest {
    @Test
    fun `material proposal requires explicit confirmation before any tool execution`() = runTest {
        var executions = 0
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult {
                executions++
                return AiToolResult.Success(emptyList())
            }
        }
        val provider = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(
                command = AiCommand.ReorganizeDay(request.message),
                explanation = "structured",
                requiresConfirmation = true,
            )
        }
        val assistant = AiAssistant(provider, gateway)

        val proposal = assistant.propose(AiRequest("estou 40 minutos atrasada"))
        val awaiting = assistant.execute(proposal)

        assertTrue(awaiting is AiExecution.AwaitingConfirmation)
        assertEquals(0, executions)
        assertTrue(assistant.execute(proposal, AiConfirmation.Confirmed) is AiExecution.Executed)
        assertEquals(1, executions)
    }

    @Test
    fun `provider can be replaced without changing assistant`() = runTest {
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        }
        val first = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(
                AiCommand.ExplainNextActivity("one"), "one", false,
            )
        }
        val second = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(
                AiCommand.ExplainNextActivity("two"), "two", false,
            )
        }

        val firstProposal = AiAssistant(first, gateway).propose(AiRequest("x"))
        val secondProposal = AiAssistant(second, gateway).propose(AiRequest("x"))

        assertEquals(AiCommand.ExplainNextActivity("one"), firstProposal.command)
        assertEquals(AiCommand.ExplainNextActivity("two"), secondProposal.command)
    }

    @Test
    fun `free text is never executed directly by the assistant`() = runTest {
        var received: AiCommand? = null
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult {
                received = command
                return AiToolResult.Success(emptyList())
            }
        }
        val provider = RuleBasedAiProvider()
        val assistant = AiAssistant(provider, gateway)

        val result = assistant.execute(assistant.propose(AiRequest("amanhã preciso estudar francês")))

        assertTrue(result is AiExecution.AwaitingConfirmation)
        assertTrue(received == null)
    }
}