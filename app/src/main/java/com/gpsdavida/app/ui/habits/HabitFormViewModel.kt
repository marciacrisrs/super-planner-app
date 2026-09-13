package com.superplanner.app.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.usecase.DeleteHabit
import com.superplanner.app.domain.usecase.GetHabit
import com.superplanner.app.domain.usecase.SaveHabit
import com.superplanner.app.ui.navigation.SuperPlannerRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitFormUiState(
    val isNew: Boolean = true,
    val title: String = "",
    val durationMinutes: String = "15",
    val priority: Priority = Priority.IMPORTANT,
    val days: Set<DayOfWeek> = emptySet(),
    val hasWindow: Boolean = false,
    val windowStart: LocalTime = LocalTime.of(7, 0),
    val windowEnd: LocalTime = LocalTime.of(9, 0),
    val error: HabitFormError? = null,
    val finished: Boolean = false,
)

enum class HabitFormError { BLANK_TITLE, INVALID_DURATION, INVALID_WINDOW }

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHabit: GetHabit,
    private val saveHabit: SaveHabit,
    private val deleteHabit: DeleteHabit,
) : ViewModel() {

    private val habitIdArg: String = checkNotNull(savedStateHandle["habitId"])
    private val habitId: HabitId =
        if (habitIdArg == SuperPlannerRoutes.NEW_HABIT_ID) HabitId(UUID.randomUUID().toString())
        else HabitId(habitIdArg)

    private val _state = MutableStateFlow(HabitFormUiState())
    val state: StateFlow<HabitFormUiState> = _state.asStateFlow()

    init {
        if (habitIdArg != SuperPlannerRoutes.NEW_HABIT_ID) {
            viewModelScope.launch {
                val existing = getHabit(habitId) ?: return@launch
                _state.value = HabitFormUiState(
                    isNew = false,
                    title = existing.title,
                    durationMinutes = existing.plannedDuration.toMinutes().toString(),
                    priority = existing.priority,
                    days = existing.daysOfWeek,
                    hasWindow = existing.window != null,
                    windowStart = existing.window?.start ?: LocalTime.of(7, 0),
                    windowEnd = existing.window?.end ?: LocalTime.of(9, 0),
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value, error = null) }
    }

    fun onDurationChange(value: String) {
        _state.update { it.copy(durationMinutes = value.filter { ch -> ch.isDigit() }, error = null) }
    }

    fun onPriority(priority: Priority) {
        _state.update { it.copy(priority = priority) }
    }

    fun toggleDay(day: DayOfWeek) {
        _state.update { current ->
            val days = current.days.toMutableSet()
            if (!days.add(day)) days.remove(day)
            current.copy(days = days)
        }
    }

    fun setHasWindow(enabled: Boolean) {
        _state.update { it.copy(hasWindow = enabled, error = null) }
    }

    fun onWindowStart(time: LocalTime) {
        _state.update { it.copy(windowStart = time, error = null) }
    }

    fun onWindowEnd(time: LocalTime) {
        _state.update { it.copy(windowEnd = time, error = null) }
    }

    fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(error = HabitFormError.BLANK_TITLE) }
            return
        }
        val minutes = current.durationMinutes.toLongOrNull()
        if (minutes == null || minutes <= 0) {
            _state.update { it.copy(error = HabitFormError.INVALID_DURATION) }
            return
        }
        val window = if (current.hasWindow) {
            runCatching { LocalTimeWindow(current.windowStart, current.windowEnd) }.getOrNull()
        } else {
            null
        }
        if (current.hasWindow && window == null) {
            _state.update { it.copy(error = HabitFormError.INVALID_WINDOW) }
            return
        }
        viewModelScope.launch {
            saveHabit(
                Habit(
                    id = habitId,
                    title = current.title.trim(),
                    plannedDuration = Duration.ofMinutes(minutes),
                    daysOfWeek = current.days,
                    window = window,
                    priority = current.priority,
                ),
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteHabit(habitId)
            _state.update { it.copy(finished = true) }
        }
    }
}
