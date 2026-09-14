package com.superplanner.app.ui.planejamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.ai.OrganizeWeekGatewayClient
import com.superplanner.app.domain.ai.OrganizeWeekPlanItem
import com.superplanner.app.domain.ai.OrganizeWeekRequest
import com.superplanner.app.domain.ai.OrganizeWeekResponse
import com.superplanner.app.domain.model.WeeklyActivityKind
import com.superplanner.app.domain.model.WeeklyPlanning
import com.superplanner.app.domain.usecase.ObserveWeeklyPlanning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OrganizeWeekState {
    data object Idle : OrganizeWeekState
    data object Loading : OrganizeWeekState
    data class Ready(val response: OrganizeWeekResponse, val week: WeeklyPlanning) : OrganizeWeekState
    data class Applied(val response: OrganizeWeekResponse, val week: WeeklyPlanning) : OrganizeWeekState
    data class Error(val message: String) : OrganizeWeekState
}

@HiltViewModel
class OrganizeWeekViewModel @Inject constructor(
    private val observeWeeklyPlanning: ObserveWeeklyPlanning,
    private val gateway: OrganizeWeekGatewayClient,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow<OrganizeWeekState>(OrganizeWeekState.Idle)
    val state: StateFlow<OrganizeWeekState> = _state.asStateFlow()

    fun organize(weekStart: LocalDate = LocalDate.now(clock).with(java.time.DayOfWeek.MONDAY)) {
        viewModelScope.launch {
            _state.value = OrganizeWeekState.Loading
            runCatching {
                val week = observeWeeklyPlanning(weekStart).first()
                val response = gateway.organize(buildRequest(week))
                _state.value = OrganizeWeekState.Ready(response, week)
            }.onFailure { error ->
                _state.value = OrganizeWeekState.Error(error.message ?: "Não foi possível organizar a semana.")
            }
        }
    }

    fun apply() {
        val current = _state.value
        if (current is OrganizeWeekState.Ready) {
            _state.value = OrganizeWeekState.Applied(current.response, current.week)
        }
    }

    private fun buildRequest(week: WeeklyPlanning): OrganizeWeekRequest {
        val items = week.activities.map { activity -> activity.toGatewayItem() }
        return OrganizeWeekRequest(
            weekStart = week.startDate.toString(),
            timezone = clock.zone.id,
            existingPlan = items,
            fixedCommitments = items.filter { it.required },
            desires = items.filter { !it.required && it.kind == WeeklyActivityKind.TASK.name },
            aiTips = emptyList(),
        )
    }

    private fun com.superplanner.app.domain.model.WeeklyActivity.toGatewayItem(): OrganizeWeekPlanItem {
        val instance = instance
        return OrganizeWeekPlanItem(
            id = instance.id.value,
            title = title,
            date = date.toString(),
            startTime = instance.planned.start.atZone(clock.zone).toLocalTime().toString(),
            endTime = instance.planned.end.atZone(clock.zone).toLocalTime().toString(),
            durationMinutes = instance.plannedDuration.toMinutes().toInt(),
            priority = instance.priority?.name,
            kind = kind.name,
            required = instance.flexibility == com.superplanner.app.domain.model.Flexibility.FIXED,
        )
    }
}
