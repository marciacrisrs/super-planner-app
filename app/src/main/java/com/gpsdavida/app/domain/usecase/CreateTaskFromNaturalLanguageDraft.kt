package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.NaturalLanguageActivityDraft
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.port.TaskRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

sealed interface CreateTaskFromNaturalLanguageResult {
    data class Created(val task: Task) : CreateTaskFromNaturalLanguageResult
    data class NeedsConfirmation(val reason: String) : CreateTaskFromNaturalLanguageResult
    data class NeedsMoreInformation(val fields: Set<String>) : CreateTaskFromNaturalLanguageResult
}

class CreateTaskFromNaturalLanguageDraft @Inject constructor(
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(
        draft: NaturalLanguageActivityDraft,
        confirmed: Boolean,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): CreateTaskFromNaturalLanguageResult {
        if (!confirmed) return CreateTaskFromNaturalLanguageResult.NeedsConfirmation(
            "A atividade interpretada precisa ser confirmada antes de ser persistida.",
        )
        if (draft.missingFields.isNotEmpty()) {
            return CreateTaskFromNaturalLanguageResult.NeedsMoreInformation(
                draft.missingFields.map { it.name }.toSet(),
            )
        }
        val duration = draft.plannedDuration ?: Duration.ZERO
        val due = draft.date?.let { date ->
            val time = draft.startTime ?: java.time.LocalTime.MAX
            date.atTime(time).atZone(zoneId).toInstant()
        }
        val task = Task(
            id = TaskId("nl-$now"),
            title = draft.title,
            plannedDuration = duration,
            priority = draft.priority,
            due = due,
            energy = draft.energy,
        )
        tasks.save(task)
        return CreateTaskFromNaturalLanguageResult.Created(task)
    }
}
