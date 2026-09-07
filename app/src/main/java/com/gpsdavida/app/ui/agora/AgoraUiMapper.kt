package com.gpsdavida.app.ui.agora

import com.gpsdavida.app.domain.model.ActivityInstance
import com.gpsdavida.app.domain.model.ActivityStatus
import com.gpsdavida.app.domain.model.DailyActivity
import com.gpsdavida.app.domain.model.NextActionDecision
import com.gpsdavida.app.domain.model.NextActionReason
import com.gpsdavida.app.domain.model.Priority
import com.gpsdavida.app.ui.next.NextActionState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class AgoraUpcomingItem(
    val title: String,
    val scheduledTime: LocalTime,
    val durationMinutes: Long,
)

data class AgoraUiState(
    val currentTime: LocalTime = LocalTime.MIDNIGHT,
    val currentDate: LocalDate = LocalDate.EPOCH,
    val title: String = "",
    val durationMinutes: Long? = null,
    val scheduledTime: LocalTime? = null,
    val priority: Priority? = null,
    val nextUpcoming: AgoraUpcomingItem? = null,
    val laterUpcoming: List<AgoraUpcomingItem> = emptyList(),
    val state: NextActionState = NextActionState.Empty,
    val currentActivity: ActivityInstance? = null,
    val reasons: List<NextActionReason> = emptyList(),
)

object AgoraUiMapper {
    fun map(
        activities: List<DailyActivity>,
        decision: NextActionDecision,
        now: Instant,
        zoneId: ZoneId,
    ): AgoraUiState {
        val recommended = decision.recommended
        val base = AgoraUiState(
            currentTime = now.atZone(zoneId).toLocalTime(),
            currentDate = now.atZone(zoneId).toLocalDate(),
        )
        if (recommended == null || recommended.status != ActivityStatus.PENDING) {
            val hasPending = activities.any { it.instance.status == ActivityStatus.PENDING }
            return base.copy(state = if (hasPending) NextActionState.Empty else NextActionState.Completed)
        }

        val current = activities.first { it.instance.id == recommended.id }
        val (nextUpcoming, laterUpcoming) = buildUpcoming(activities, recommended, decision.next, zoneId)

        return base.copy(
            title = current.title,
            durationMinutes = recommended.plannedDuration.toMinutes(),
            scheduledTime = recommended.planned.start.atZone(zoneId).toLocalTime(),
            priority = recommended.priority,
            nextUpcoming = nextUpcoming,
            laterUpcoming = laterUpcoming,
            state = NextActionState.Ready,
            currentActivity = recommended,
            reasons = decision.recommendedReasons,
        )
    }

    fun buildUpcoming(
        activities: List<DailyActivity>,
        recommended: ActivityInstance,
        next: ActivityInstance?,
        zoneId: ZoneId,
    ): Pair<AgoraUpcomingItem?, List<AgoraUpcomingItem>> {
        val pending = activities.filter { it.instance.status == ActivityStatus.PENDING }.sortedBy { it.instance.planned.start }
        val excluded = mutableSetOf(recommended.id)
        val nextUpcoming = next
            ?.takeIf { it.status == ActivityStatus.PENDING && it.id != recommended.id }
            ?.let { instance ->
                excluded.add(instance.id)
                pending.first { it.instance.id == instance.id }.toUpcoming(zoneId)
            }
        val laterUpcoming = pending.filter { it.instance.id !in excluded }.take(3).map { it.toUpcoming(zoneId) }
        return nextUpcoming to laterUpcoming
    }

    private fun DailyActivity.toUpcoming(zoneId: ZoneId) = AgoraUpcomingItem(
        title = title,
        scheduledTime = instance.planned.start.atZone(zoneId).toLocalTime(),
        durationMinutes = instance.plannedDuration.toMinutes(),
    )
}
