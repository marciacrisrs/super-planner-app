package com.gpsdavida.app.ui.agora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.domain.model.NextActionContext
import com.gpsdavida.app.domain.usecase.ChooseNextActivity
import com.gpsdavida.app.domain.usecase.CompleteActivityInstance
import com.gpsdavida.app.domain.usecase.DeferActivityInstance
import com.gpsdavida.app.domain.usecase.ObserveExecutableDay
import com.gpsdavida.app.domain.usecase.RecalculateRoute
import com.gpsdavida.app.domain.usecase.SkipActivityInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AgoraViewModel @Inject constructor(
    observeExecutableDay: ObserveExecutableDay,
    private val chooseNextActivity: ChooseNextActivity,
    private val recalculateRoute: RecalculateRoute,
    private val completeActivity: CompleteActivityInstance,
    private val skipActivity: SkipActivityInstance,
    private val deferActivity: DeferActivityInstance,
    private val clock: Clock,
) : ViewModel() {
    private val nowFlow = flow {
        emit(clock.instant())
        while (true) {
            delay(60_000L)
            emit(clock.instant())
        }
    }

    val state: StateFlow<AgoraUiState> = combine(observeExecutableDay(), nowFlow) { activities, now ->
        val instances = activities.map { it.instance }
        val recalculated = recalculateRoute(
            activities = instances,
            date = now.atZone(clock.zone).toLocalDate(),
            zoneId = clock.zone,
            now = now,
        )
        val decision = chooseNextActivity(
            recalculated.activities,
            NextActionContext(now = now, zoneId = clock.zone),
        )
        AgoraUiMapper.map(
            activities = activities.map { daily ->
                val scheduled = recalculated.activities.firstOrNull { it.id == daily.instance.id }
                if (scheduled == null) daily else daily.copy(instance = scheduled)
            },
            decision = decision,
            now = now,
            zoneId = clock.zone,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgoraUiState())

    fun completeCurrent() {
        val activity = state.value.currentActivity ?: return
        viewModelScope.launch { completeActivity(activity) }
    }

    fun skipCurrent() {
        val activity = state.value.currentActivity ?: return
        viewModelScope.launch { skipActivity(activity) }
    }

    fun deferCurrent() {
        val activity = state.value.currentActivity ?: return
        viewModelScope.launch { deferActivity(activity) }
    }
}
