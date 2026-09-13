package com.superplanner.app.ui.agora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.usecase.ChooseNextActivity
import com.superplanner.app.domain.usecase.CompleteActivityInstance
import com.superplanner.app.domain.usecase.DeferActivityInstance
import com.superplanner.app.domain.usecase.ObserveExecutableDay
import com.superplanner.app.domain.usecase.RecalculateRoute
import com.superplanner.app.domain.usecase.SkipActivityInstance
import com.superplanner.app.domain.usecase.StartActivityInstance
import com.superplanner.app.ui.notifications.ActivityNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val startActivity: StartActivityInstance,
    private val completeActivity: CompleteActivityInstance,
    private val skipActivity: SkipActivityInstance,
    private val deferActivity: DeferActivityInstance,
    private val clock: Clock,
) : AndroidViewModel(application) {
    private val nowFlow = flow {
        emit(clock.instant())
        while (true) { delay(60_000L); emit(clock.instant()) }
    }
    private val recentlyCompleted = MutableStateFlow<com.superplanner.app.domain.model.ActivityInstance?>(null)

    val state: StateFlow<AgoraUiState> = combine(observeExecutableDay(), nowFlow, recentlyCompleted) { activities, now, completed ->
        val recalculated = recalculateRoute(
            activities.map { it.instance },
            now.atZone(clock.zone).toLocalDate(),
            zoneId = clock.zone,
            now = now,
            delayedActivity = completed,
        )
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

    fun startCurrent() {
        state.value.currentActivity?.takeIf { it.status == ActivityStatus.PENDING }?.let { activity ->
            viewModelScope.launch { startActivity(activity) }
        }
    }

    fun completeCurrent() {
        state.value.currentActivity?.takeIf { it.status == ActivityStatus.IN_PROGRESS }?.let { activity ->
            viewModelScope.launch {
                recentlyCompleted.value = completeActivity(activity)
            }
        }
    }

    fun skipCurrent() {
        state.value.currentActivity?.takeIf { it.status == ActivityStatus.PENDING }?.let { activity ->
            viewModelScope.launch { skipActivity(activity) }
        }
    }

    fun deferCurrent() {
        state.value.currentActivity?.takeIf { it.status == ActivityStatus.PENDING }?.let { activity ->
            viewModelScope.launch { deferActivity(activity) }
        }
    }
}
