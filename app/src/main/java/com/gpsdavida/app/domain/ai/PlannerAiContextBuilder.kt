package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyActivity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/** Builds the smallest useful planner context for AI without exposing persistence details. */
class PlannerAiContextBuilder @Inject constructor(
    private val clock: Clock,
) {
    fun build(
        activities: List<DailyActivity>,
        activeActivityId: String? = null,
        now: Instant = clock.instant(),
    ): AiContext {
        val today = now.atZone(clock.zone).toLocalDate()
        val facts = buildList {
            add("date=$today")
            add("time=${now.atZone(clock.zone).toLocalTime().withSecond(0).withNano(0)}")

            activities
                .asSequence()
                .filter { it.instance.status == ActivityStatus.IN_PROGRESS }
                .take(1)
                .forEach { add("active=${it.instance.id};title=${it.title}") }

            activities
                .asSequence()
                .filter { it.instance.status == ActivityStatus.PENDING }
                .sortedBy { it.instance.planned }
                .take(8)
                .forEach { activity ->
                    val instance = activity.instance
                    add(
                        "activity=${instance.id};title=${activity.title};" +
                            "status=${instance.status};planned=${instance.planned};" +
                            "durationMinutes=${instance.plannedDuration.toMinutes()};" +
                            "priority=${instance.priority};flexibility=${instance.flexibility}",
                    )
                }
        }

        return AiContext(
            nowIso = now.toString(),
            activeActivityId = activeActivityId
                ?: activities.firstOrNull { it.instance.status == ActivityStatus.IN_PROGRESS }?.instance?.id?.toString(),
            minimalRouteFacts = facts.take(12),
        )
    }
}
