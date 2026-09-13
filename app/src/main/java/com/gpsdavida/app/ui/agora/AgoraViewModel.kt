package com.superplanner.app.ui.agora

import android.app.Application
import android.os.Trace
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.DailyCapacity
import com.superplanner.app.domain.model.NextActionContext
import com.superplanner.app.domain.model.RouteFeedbackReason
import com.superplanner.app.domain.planning.LowCapacityPlanner
import com.superplanner.app.domain.port.ActivityExecutionRepository
import com.superplanner.app.domain.usecase.ChooseNextActivity
import com.superplanner.app.domain.usecase.CompleteActivityInstance
import com.superplanner.app.domain.usecase.DeferActivityInstance
import com.superplanner.app.domain.usecase.LearnActivityDurations
import com.superplanner.app.domain.usecase.ObserveExecutableDay
import com.superplanner.app.domain.usecase.RecalculateRoute
import com.superplanner.app.domain.usecase.RecordRouteFeedback
import com.superplanner.app.domain.usecase.SkipActivityInstance
import com.superplanner.app.domain.usecase.StartActivityInstance
import com.superplanner.app.ui.notifications.ActivityNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AgoraViewModel @Inject constructor(
    application: Application,
    observeExecutableDay: ObserveExecutableDay,
    executionRepository: ActivityExecutionRepository,
    learnActivityDurations: LearnActivityDurations,
    private val chooseNextActivity: ChooseNextActivity,
    private val recalculateRoute: RecalculateRoute,
    private val startActivity: StartActivityInstance,
    private val completeActivity: CompleteActivityInstance,
    private val skipActivity: SkipActivityInstance,
    private val deferActivity: DeferActivityInstance,
    private val recordRouteFeedback: RecordRouteFeedback,
    private val clock: Clock,
) : AndroidViewModel(application) {
    private val nowFlow = flow {
        emit(clock.instant())
        while (true) { delay(60_000L); emit(clock.instant()) }
    }
    private val recentlyCompleted = MutableStateFlow<com.superplanner.app.domain.model.ActivityInstance?>(null)
    private val lowCapacityMode = MutableStateFlow(false)
    private val learnedDurations = executionRepository.observeAll()
        .map(learnActivityDurations::invoke)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private data class PlannerSnapshot(
        val activities: List<ExecutableActivityUi>,
        val now: java.time.Instant,
        val completed: com.superplanner.app.domain.model.ActivityInstance?,
        val lowCapacity: Boolean,
    )

    private val plannerSnapshot = combine(
        observeExecutableDay(),
        nowFlow,
        recentlyCompleted,
        lowCapacityMode,
    ) { activities, now, completed, lowCapacity ->
        PlannerSnapshot(activities, now, completed, lowCapacity)
    }

    val state: StateFlow<AgoraUiState> = combine(plannerSnapshot, learnedDurations) { snapshot, learned ->
        val original = snapshot.activities.map { it.instance }
        val dailyCapacity: DailyCapacity? = snapshot.lowCapacity.let { active -> if (active) LowCapacityPlanner.capacity() else null }
        val recalculated = withContext(Dispatchers.Default) {
            trace("SuperPlanner.Planning") {
                recalculateRoute(
                    original,
                    snapshot.now.atZone(clock.zone).toLocalDate(),
                    zoneId = clock.zone,
                    now = snapshot.now,
                    delayedActivity = snapshot.completed,
                    dailyCapacity = dailyCapacity,
                    learnedDurations = learned,
                )
            }
        }
        val decision = withContext(Dispatchers.Default) {
            trace("SuperPlanner.ChooseNext") {
                chooseNextActivity(
                    recalculated.activities,
                    NextActionContext(
                        now = snapshot.now,
                        zoneId = clock.zone,
                        dailyCapacity = dailyCapacity,
                        learnedDurations = learned,
                    ),
                )
            }
        }
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
        val mappedActivities = snapshot.activities.map { daily ->
            recalculated.activities.firstOrNull { it.id == daily.instance.id }?.let { daily.copy(instance = it) } ?: daily
        }
        val mapped = AgoraUiMapper.map(
            mappedActivities,
            decision,
            snapshot.now,
            clock.zone,
            lowCapacity = snapshot.lowCapacity,
            lowCapacitySummary = summary,
        )
        decision.recommended?.let { ActivityNotificationScheduler.schedule(getApplication(), it) }
        mapped
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgoraUiState())

    fun setLowCapacity(enabled: Boolean) {
        lowCapacityMode.value = enabled
    }

    fun recordCurrentFeedback(reason: RouteFeedbackReason) {
        state.value.currentActivity?.let { activity ->
            viewModelScope.launch {
                recordRouteFeedback(activity.id, reason)
            }
        }
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

    private inline fun <T> trace(name: String, block: () -> T): T {
        Trace.beginSection(name)
        return try {
            block()
        } finally {
            Trace.endSection()
        }
    }
}
