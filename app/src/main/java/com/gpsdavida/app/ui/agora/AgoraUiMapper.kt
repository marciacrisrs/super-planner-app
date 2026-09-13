package com.superplanner.app.ui.agora

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.model.NextActionDecision
import com.superplanner.app.domain.model.NextActionReason
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.ui.next.NextActionState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class AgoraUpcomingItem(
    val title: String,
    val scheduledTime: LocalTime,
    val durationMinutes: Long,
    val explanation: String? = null,
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
    val explanation: String? = null,
)

object AgoraUiMapper {
    fun map(
        activities: List<DailyActivity>,
        decision: NextActionDecision,
        now: Instant,
        zoneId: ZoneId,
    ): AgoraUiState {
        val running = activities.firstOrNull { it.instance.status == ActivityStatus.IN_PROGRESS }
        val recommendedId = running?.instance?.id ?: decision.recommended?.id
        val base = AgoraUiState(
            currentTime = now.atZone(zoneId).toLocalTime(),
            currentDate = now.atZone(zoneId).toLocalDate(),
        )

        if (recommendedId == null) {
            val hasPending = activities.any { it.instance.status == ActivityStatus.PENDING }
            return base.copy(state = if (hasPending) NextActionState.Empty else NextActionState.Completed)
        }

        val current = activities.first { it.instance.id == recommendedId }
        val recommended = current.instance
        val reasons = if (running != null) {
            listOf(NextActionReason.CURRENTLY_ACTIVE)
        } else {
            decision.recommendedReasons
        }
        val nextForUpcoming = if (running == null) decision.next else null
        val (nextUpcoming, laterUpcoming) = buildUpcoming(
            activities = activities,
            recommended = recommended,
            next = nextForUpcoming,
            zoneId = zoneId,
            deferralExplanation = deferralExplanation(recommended, decision.next, now),
        )

        return base.copy(
            title = current.title,
            durationMinutes = recommended.plannedDuration.toMinutes(),
            scheduledTime = recommended.planned.start.atZone(zoneId).toLocalTime(),
            priority = recommended.priority,
            nextUpcoming = nextUpcoming,
            laterUpcoming = laterUpcoming,
            state = when (recommended.status) {
                ActivityStatus.IN_PROGRESS -> NextActionState.InProgress
                ActivityStatus.DONE -> NextActionState.Completed
                else -> NextActionState.Ready
            },
            currentActivity = recommended,
            reasons = reasons,
            explanation = explanationFor(reasons),
        )
    }

    fun buildUpcoming(
        activities: List<DailyActivity>,
        recommended: ActivityInstance,
        next: ActivityInstance?,
        zoneId: ZoneId,
        deferralExplanation: String? = null,
    ): Pair<AgoraUpcomingItem?, List<AgoraUpcomingItem>> {
        val pending = activities
            .filter { it.instance.status == ActivityStatus.PENDING }
            .sortedBy { it.instance.planned.start }
        val excluded = mutableSetOf(recommended.id)
        val nextUpcoming = next
            ?.takeIf { it.status == ActivityStatus.PENDING && it.id != recommended.id }
            ?.let { instance ->
                excluded.add(instance.id)
                pending.firstOrNull { it.instance.id == instance.id }?.toUpcoming(zoneId, deferralExplanation)
            }
        val laterUpcoming = pending
            .filter { it.instance.id !in excluded }
            .take(3)
            .map { it.toUpcoming(zoneId) }
        return nextUpcoming to laterUpcoming
    }

    private fun DailyActivity.toUpcoming(zoneId: ZoneId, explanation: String? = null) = AgoraUpcomingItem(
        title = title,
        scheduledTime = instance.planned.start.atZone(zoneId).toLocalTime(),
        durationMinutes = instance.plannedDuration.toMinutes(),
        explanation = explanation,
    )

    private fun explanationFor(reasons: List<NextActionReason>): String? = when {
        reasons.contains(NextActionReason.HIGHER_PRIORITY) && reasons.contains(NextActionReason.DUE_NOW) ->
            "Escolhi agora porque tem prioridade mais alta e já pode ser feita."
        reasons.contains(NextActionReason.HIGHER_PRIORITY) ->
            "Escolhi agora porque tem prioridade mais alta entre as opções que cabem neste momento."
        reasons.contains(NextActionReason.FIXED_COMMITMENT) ->
            "Escolhi agora porque é um compromisso fixo."
        reasons.contains(NextActionReason.CAPACITY_AVAILABLE) ->
            "Escolhi agora porque ainda há espaço na sua capacidade hoje."
        reasons.contains(NextActionReason.AVAILABLE_IN_WINDOW) ->
            "Escolhi agora porque cabe na sua janela disponível."
        reasons.contains(NextActionReason.ENERGY_MATCH) ->
            "Escolhi agora porque combina com sua energia atual."
        reasons.contains(NextActionReason.CONTEXT_MATCH) ->
            "Escolhi agora porque combina com o seu contexto."
        reasons.contains(NextActionReason.TRAVEL_FITS) ->
            "Escolhi agora porque o deslocamento cabe no tempo disponível."
        reasons.contains(NextActionReason.DEPENDENCIES_SATISFIED) ->
            "Escolhi agora porque as dependências necessárias já foram atendidas."
        reasons.contains(NextActionReason.DUE_NOW) ->
            "Escolhi agora porque já pode ser feita."
        reasons.contains(NextActionReason.CURRENTLY_ACTIVE) ->
            "Você já está fazendo esta atividade."
        else -> null
    }

    private fun deferralExplanation(
        current: ActivityInstance,
        next: ActivityInstance?,
        now: Instant,
    ): String? {
        next ?: return null
        return when {
            current.priority.weight < next.priority.weight ->
                "Deixei para depois porque esta atividade tem prioridade maior."
            current.planned.start <= now && next.planned.start > now ->
                "Deixei para depois porque esta atividade já pode ser feita agora."
            current.flexibility.name == "FIXED" ->
                "Deixei para depois para respeitar este compromisso."
            else ->
                "Deixei para depois para manter a sequência mais adequada para agora."
        }
    }
}
