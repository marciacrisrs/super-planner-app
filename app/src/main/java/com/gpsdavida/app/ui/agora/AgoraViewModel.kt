package com.superplanner.app.ui.agora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.planning.LowCapacityPlanner
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
    private val lowCapacityMode = MutableStateFlow(false)

    val state: StateFlow<AgoraUiState> = combine(observeExecutableDay(), nowFlow, recentlyCompleted, lowCapacityMode) { activities, now, completed, lowCapacity ->
        val original = activities.map { it.instance }
        val dailyCapacity: DailyCapacity? = lowCapacity.let { active -> if (active) LowCapacityPlanner.capacity() else null }
        val recalculated = recalculateRoute(
            original,
            now.atZone(clock.zone).toLocalDate(),
            zoneId = clock.zone,
            now = now,
            delayedActivity = completed,
            dailyCapacity = dailyCapacity,
        )
        val decision = chooseNextActivity(
            recalculated.activities,
            NextActionContext(now = now, zoneId = clock.zone, dailyCapacity = dailyCapacity),
        )
        val preserved = recalculated.activities.count {
            it.flexibility.name == "FIXED" || it.priority.weight == 0
        }
        val moved = recalculated.activities.count { current ->
            original.firstOrNull { it.id == current.id }?.planned != current.planned
        }
        val summary = LowCapacitySummary(
            preserved = preserved,
            moved = moved,
            deferred = recalculated.conflicts.size,
        )
        val mappedActivities = activities.map { daily ->
            recalculated.activities.firstOrNull { it.id == daily.instance.id }?.let { daily.copy(instance = it) } ?: daily
        }
        val mapped = AgoraUiMapper.map(
            mappedActivities,
            decision,
            now,
            clock.zone,
            lowCapacity = lowCapacity,
            lowCapacitySummary = summary,
        )
        decision.recommended?.let { ActivityNotificationScheduler.schedule(getApplication(), it) }
        mapped
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgoraUiState())

    fun setLowCapacity(enabled: Boolean) {
        lowCapacityMode.value = enabled
    }

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
