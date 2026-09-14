package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.AiToolGateway
import com.superplanner.app.domain.port.AiToolResult
import com.superplanner.app.domain.planning.DayReorganizationOperation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

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
                command = AiCommand.ReorganizeDay(
                    com.superplanner.app.domain.planning.DayReorganizationRequest(
                        operation = DayReorganizationOperation.DelayActivity(ActivityInstanceId("activity"), 40),
                        now = Instant.parse("2026-09-13T19:00:00Z"),
                    ),
                ),
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
                AiCommand.ExplainNextActivity("one", listOf("priority: high")), "one", false,
            )
        }
        val second = object : AiProvider {
            override suspend fun interpret(request: AiRequest) = AiProposal(
                AiCommand.ExplainNextActivity("two", listOf("due: now")), "two", false,
            )
        }

        val firstProposal = AiAssistant(first, gateway).propose(AiRequest("x"))
        val secondProposal = AiAssistant(second, gateway).propose(AiRequest("x"))

        assertEquals(AiCommand.ExplainNextActivity("one", listOf("priority: high")), firstProposal.command)
        assertEquals(AiCommand.ExplainNextActivity("two", listOf("due: now")), secondProposal.command)
    }

    @Test
    fun `natural language is converted to structured draft without persisting it`() = runTest {
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        }
        val assistant = AiAssistant(RuleBasedAiProvider(), gateway)

        val proposal = assistant.propose(
            AiRequest(
                message = "amanhã preciso estudar francês por uma hora depois do trabalho",
                context = AiContext(nowIso = "2026-09-13T19:00:00Z"),
            ),
        )

        val command = proposal.command as AiCommand.CreateActivityDraft
        assertEquals("francês", command.draft.title)
        assertEquals(Duration.ofHours(1), command.draft.plannedDuration)
        assertEquals("2026-09-14", command.draft.date.toString())
        assertTrue(proposal.requiresConfirmation)
    }

    @Test
    fun `late request becomes a structured reorganization operation`() = runTest {
        val assistant = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        })

        val proposal = assistant.propose(
            AiRequest(
                message = "estou 40 minutos atrasada, reorganize",
                context = AiContext(
                    nowIso = "2026-09-13T19:00:00Z",
                    activeActivityId = "activity-123",
                ),
            ),
        )

        val command = proposal.command as AiCommand.ReorganizeDay
        assertTrue(command.request.operation is DayReorganizationOperation.DelayActivity)
        val delay = command.request.operation as DayReorganizationOperation.DelayActivity
        assertEquals(ActivityInstanceId("activity-123"), delay.activityId)
        assertEquals(40, delay.minutes)
        assertTrue(proposal.requiresConfirmation)
    }

    @Test
    fun `reorganization asks for missing information instead of guessing`() = runTest {
        val assistant = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        })

        val proposal = assistant.propose(
            AiRequest(
                message = "estou atrasada, reorganize",
                context = AiContext(nowIso = "2026-09-13T19:00:00Z"),
            ),
        )

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
        val domainExplanation = com.superplanner.app.domain.usecase.ExplainNextActivity()(
            NextActionDecision(
                current = null,
                next = activity,
                nextReasons = listOf(NextActionReason.HIGHER_PRIORITY, NextActionReason.CAPACITY_AVAILABLE),
            ),
        )
        val evidence = listOf("priority: high", "capacity: available")
        val assistant = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        })

        val proposal = assistant.propose(
            AiRequest(
                message = "por que estudar agora?",
                context = AiContext(
                    activeActivityId = "study",
                    minimalRouteFacts = evidence,
                ),
            ),
        )

        val command = proposal.command as AiCommand.ExplainNextActivity
        assertEquals("study", command.activityId)
        assertEquals(2, command.evidence.size)
        assertEquals(evidence, command.evidence)
        assertTrue(proposal.explanation.isNotBlank())
        assertTrue(domainExplanation.facts.isNotEmpty())
        assertTrue(!proposal.requiresConfirmation)
    }

    @Test
    fun `why question declines to explain without evidence`() = runTest {
        val assistant = AiAssistant(RuleBasedAiProvider(), object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult = AiToolResult.Success(emptyList())
        })

        val proposal = assistant.propose(
            AiRequest("por que isso agora?"),
        )

        assertTrue(proposal.command is AiCommand.MissingInformation)
        assertTrue((proposal.command as AiCommand.MissingInformation).fields.contains("evidências da decisão atual"))
    }

    @Test
    fun `free text never reaches tool execution before confirmation`() = runTest {
        var received: AiCommand? = null
        val gateway = object : AiToolGateway {
            override suspend fun execute(command: AiCommand): AiToolResult {
                received = command
                return AiToolResult.Success(emptyList())
            }
        }
        val assistant = AiAssistant(RuleBasedAiProvider(), gateway)

        val result = assistant.execute(
            assistant.propose(
                AiRequest(
                    "amanhã estudar francês por uma hora",
                    AiContext(nowIso = "2026-09-13T19:00:00Z"),
                ),
            ),
        )

        assertTrue(result is AiExecution.AwaitingConfirmation)
        assertEquals(null, received)
    }
}
