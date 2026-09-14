package com.gpsdavida.app.domain.ai

import com.gpsdavida.app.domain.model.ActivityInstance
import com.gpsdavida.app.domain.model.ActivityInstanceId
import com.gpsdavida.app.domain.model.ActivitySource
import com.gpsdavida.app.domain.model.Flexibility
import com.gpsdavida.app.domain.model.NextActionDecision
import com.gpsdavida.app.domain.model.NextActionReason
import com.gpsdavida.app.domain.model.TaskId
import com.gpsdavida.app.domain.model.TimeRange
import com.gpsdavida.app.domain.port.AiToolGateway
import com.gpsdavida.app.domain.port.AiToolResult
import com.gpsdavida.app.domain.planning.DayReorganizationOperation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class AiAssistantTest {
    @Test
    fun `material proposal requires explicit confirmation before tool execution`() = runTest {
        var executions = 0
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult {
                executions++
                return AiToolResult.Success(emptyList())
            }
        }
        val provider = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(
                AiCommand.ReorganizeDay(
                    com.gpsdavida.app.domain.planning.DayReorganizationRequest(
                        DayReorganizationOperation.DelayActivity(ActivityInstanceId("activity"), 40),
                        Instant.parse("2026-09-13T19:00:00Z"),
                    ),
                ),
                "structured",
                true,
            )
        }
        val assistant = AiAssistant(provider, gateway)
        val proposal = assistant.propose(AiRequest("estou 40 minutos atrasada"))

        assertTrue(assistant.execute(proposal) is AiExecution.AwaitingConfirmation)
        assertEquals(0, executions)
        assertTrue(assistant.execute(proposal, AiConfirmation.Confirmed) is AiExecution.Executed)
        assertEquals(1, executions)
    }

    @Test
    fun `provider can be replaced without changing assistant`() = runTest {
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }
        val first = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(AiCommand.ExplainNextActivity("one", listOf("priority: high")), "one", false)
        }
        val second = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(AiCommand.ExplainNextActivity("two", listOf("due: now")), "two", false)
        }
        assertEquals("one", (AiAssistant(first, gateway).propose(AiRequest("x")).command as AiCommand.ExplainNextActivity).activityId)
        assertEquals("two", (AiAssistant(second, gateway).propose(AiRequest("x")).command as AiCommand.ExplainNextActivity).activityId)
    }

    @Test
    fun `natural language is converted to structured draft without persisting it`() = runTest {
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }
        val proposal = AiAssistant(RuleBasedAiProvider(), gateway).propose(
            AiRequest("amanhã preciso estudar francês por uma hora depois do trabalho", AiContext(nowIso = "2026-09-13T19:00:00Z")),
        )
        val command = proposal.command as AiCommand.CreateActivityDraft
        assertEquals("francês", command.draft.title)
        assertEquals(Duration.ofHours(1), command.draft.plannedDuration)
        assertEquals("2026-09-14", command.draft.date.toString())
        assertTrue(proposal.requiresConfirmation)
    }

    @Test
    fun `late request becomes a structured reorganization operation`() = runTest {
        val proposal = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }).propose(
            AiRequest("estou 40 minutos atrasada, reorganize", AiContext("2026-09-13T19:00:00Z", "activity-123")),
        )
        val command = proposal.command as AiCommand.ReorganizeDay
        val delay = command.request.operation as DayReorganizationOperation.DelayActivity
        assertEquals(ActivityInstanceId("activity-123"), delay.activityId)
        assertEquals(40, delay.minutes)
        assertTrue(proposal.requiresConfirmation)
    }

    @Test
    fun `reorganization asks for missing information instead of guessing`() = runTest {
        val proposal = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }).propose(AiRequest("estou atrasada, reorganize", AiContext(nowIso = "2026-09-13T19:00:00Z")))
        val command = proposal.command as AiCommand.MissingInformation
        assertTrue(command.fields.contains("qual atividade deve ser alterada"))
        assertTrue(command.fields.contains("quantos minutos mudou"))
        assertTrue(!proposal.requiresConfirmation)
    }

    @Test
    fun `why question receives only domain evidence`() = runTest {
        val activity = ActivityInstance(
            id = ActivityInstanceId("study"),
            source = ActivitySource.FromTask(TaskId("study-task")),
            flexibility = Flexibility.FLEXIBLE,
            planned = TimeRange(Instant.parse("2026-09-13T09:00:00Z"), Instant.parse("2026-09-13T10:00:00Z")),
        )
        val domainExplanation = com.gpsdavida.app.domain.usecase.ExplainNextActivity()(
            NextActionDecision(null, activity, listOf(NextActionReason.HIGHER_PRIORITY, NextActionReason.CAPACITY_AVAILABLE)),
        )
        val evidence = listOf("priority: high", "capacity: available")
        val proposal = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }).propose(AiRequest("por que estudar agora?", AiContext("2026-09-13T19:00:00Z", "study", evidence)))
        val command = proposal.command as AiCommand.ExplainNextActivity
        assertEquals("study", command.activityId)
        assertEquals(evidence, command.evidence)
        assertTrue(domainExplanation.facts.isNotEmpty())
        assertTrue(!proposal.requiresConfirmation)
    }

    @Test
    fun `why question declines to explain without evidence`() = runTest {
        val proposal = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult = AiToolResult.Success(emptyList())
        }).propose(AiRequest("por que isso agora?"))
        assertTrue(proposal.command is AiCommand.MissingInformation)
        assertTrue((proposal.command as AiCommand.MissingInformation).fields.contains("evidências da decisão atual"))
    }

    @Test
    fun `free text never reaches tool execution before confirmation`() = runTest {
        var received: AiCommand? = null
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult {
                received = command
                return AiToolResult.Success(emptyList())
            }
        }
        val assistant = AiAssistant(RuleBasedAiProvider(), gateway)
        val result = assistant.execute(assistant.propose(AiRequest("amanhã estudar francês por uma hora", AiContext(nowIso = "2026-09-13T19:00:00Z"))))
        assertTrue(result is AiExecution.AwaitingConfirmation)
        assertEquals(null, received)
    }

    @Test
    fun `mutating command cannot bypass confirmation when provider declares it optional`() = runTest {
        var executions = 0
        var receivedConfirmation = true
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand, confirmed: Boolean): AiToolResult {
                executions++
                receivedConfirmation = confirmed
                return AiToolResult.Success(emptyList())
            }
        }
        val parsed = RuleBasedAiProvider().interpret(AiRequest("amanhã estudar francês por uma hora", AiContext(nowIso = "2026-09-13T19:00:00Z")))
        val proposal = parsed.copy(requiresConfirmation = false)
        val assistant = AiAssistant(object : AiProvider {
            override suspend fun interpret(request: AiRequest) = proposal
        }, gateway)

        val result = assistant.execute(assistant.propose(AiRequest("estudar")))

        assertTrue(result is AiExecution.AwaitingConfirmation)
        assertEquals(0, executions)
        assertTrue(receivedConfirmation)
    }
}
