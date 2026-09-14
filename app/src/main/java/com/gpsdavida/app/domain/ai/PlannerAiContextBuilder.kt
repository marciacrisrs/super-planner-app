package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.port.ActivityExecutionRepository
import com.superplanner.app.domain.port.AvailabilityRepository
import com.superplanner.app.domain.usecase.LearnActivityDurations
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Builds the smallest useful planner context for AI without exposing persistence details. */
class PlannerAiContextBuilder @Inject constructor(
    private val clock: Clock,
    private val availabilityRepository: AvailabilityRepository,
    private val executions: ActivityExecutionRepository,
    private val learnActivityDurations: LearnActivityDurations,
) {
    suspend fun build(
        activities: List<DailyActivity>,
        activeActivityId: String? = null,
        now: Instant = clock.instant(),
    ): AiContext {
        val zone = clock.zone
        val localNow = now.atZone(zone)
        val availability = availabilityRepository.observeForDay(localNow.dayOfWeek).first()
        val learnedDurations = learnActivityDurations(executions.observeAll().first())

        val pending = activities
            .asSequence()
            .filter { it.instance.status == ActivityStatus.PENDING }
            .sortedBy { it.instance.planned.start }
            .toList()

        val facts = buildList {
            add("fact.date=${localNow.toLocalDate()}")
            add("fact.time=${localNow.toLocalTime().withSecond(0).withNano(0)}")

            activities
                .asSequence()
                .filter { it.instance.status == ActivityStatus.IN_PROGRESS }
                .take(1)
                .forEach { add("fact.active=${it.instance.id};title=${it.title}") }

            pending.take(8).forEach { activity ->
                val instance = activity.instance
                val learned = learnedDurations[instance.id]
                add(
                    "fact.activity=${instance.id};title=${activity.title};" +
                        "status=${instance.status};planned=${instance.planned};" +
                        "durationMinutes=${instance.plannedDuration.toMinutes()};" +
                        "priority=${instance.priority};flexibility=${instance.flexibility};" +
                        "learnedDurationMinutes=${learned?.toMinutes()}",
                )
            }

            availability.forEach { rule ->
                add("fact.availability=${rule.kind};${rule.window.start}-${rule.window.end}")
            }

            pending.firstOrNull { it.instance.planned.start > now }?.let { next ->
                add("fact.next=${next.instance.id};title=${next.title};starts=${next.instance.planned.start}")
            }
        }

        return AiContext(
            nowIso = now.toString(),
            activeActivityId = activeActivityId
                ?: activities.firstOrNull { it.instance.status == ActivityStatus.IN_PROGRESS }?.instance?.id?.toString(),
            minimalRouteFacts = facts.take(32),
        )
    }
}
