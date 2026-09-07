package com.gpsdavida.app.ui.agora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.domain.model.NextActionContext
import com.gpsdavida.app.domain.usecase.ChooseNextActivity
import com.gpsdavida.app.domain.usecase.CompleteActivityInstance
import com.gpsdavida.app.domain.usecase.DeferActivityInstance
import com.gpsdavida.app.domain.usecase.ObserveExecutableDay
import com.gpsdavida.app.domain.usecase.RecalculateRoute
import com.gpsdavida.app.domain.usecase.SkipActivityInstance
import com.gpsdavida.app.ui.notifications.ActivityNotificationScheduler
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
    application: Application,
    observeExecutableDay: ObserveExecutableDay,
    private val chooseNextActivity: ChooseNextActivity,
    private val recalculateRoute: RecalculateRoute,
    private val completeActivity: CompleteActivityInstance,
    private val skipActivity: SkipActivityInstance,
    private val deferActivity: DeferActivityInstance,
    private val clock: Clock,
) : AndroidViewModel(application) {
    private val nowFlow = flow {
        emit(clock.instant())
        while (true) { delay(60_000L); emit(clock.instant()) }
    }

    val state: StateFlow<AgoraUiState> = combine(observeExecutableDay(), nowFlow) { activities, now ->
        val recalculated = recalculateRoute(activities.map { it.instance }, now.atZone(clock.zone).toLocalDate(), zoneId = clock.zone, now = now)
        val decision = chooseNextActivity(recalculated.activities, NextActionContext(now = now, zoneId = clock.zone))
        val mapped = AgoraUiMapper.map(
            activities.map { daily -> recalculated.activities.firstOrNull { it.id == daily.instance.id }?.let { daily.copy(instance = it) } ?: daily },
            decision,
            now,
            clock.zone,
        )
        decision.recommended?.let { ActivityNotificationScheduler.schedule(getApplication(), it) }
        mapped
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgoraUiState())

    fun completeCurrent() { state.value.currentActivity?.let { viewModelScope.launch { completeActivity(it) } } }
    fun skipCurrent() { state.value.currentActivity?.let { viewModelScope.launch { skipActivity(it) } } }
    fun deferCurrent() { state.value.currentActivity?.let { viewModelScope.launch { deferActivity(it) } } }
}
