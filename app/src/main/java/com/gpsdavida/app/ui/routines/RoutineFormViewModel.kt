package com.superplanner.app.ui.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.RoutineId
import com.superplanner.app.domain.model.RoutineStep
import com.superplanner.app.domain.model.RoutineStepId
import com.superplanner.app.domain.usecase.DeleteRoutine
import com.superplanner.app.domain.usecase.GetRoutine
import com.superplanner.app.domain.usecase.SaveRoutine
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

data class RoutineStepFormState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val durationMinutes: String = "10",
)

data class RoutineFormUiState(
    val isNew: Boolean = true,
    val title: String = "",
    val priority: Priority = Priority.IMPORTANT,
    val days: Set<DayOfWeek> = emptySet(),
    val hasStartTime: Boolean = false,
    val startTime: LocalTime = LocalTime.of(7, 0),
    val steps: List<RoutineStepFormState> = emptyList(),
    val error: RoutineFormError? = null,
    val finished: Boolean = false,
)

enum class RoutineFormError { BLANK_TITLE, NO_STEPS, INVALID_STEP }

@HiltViewModel
class RoutineFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRoutine: GetRoutine,
    private val saveRoutine: SaveRoutine,
    private val deleteRoutine: DeleteRoutine,
) : ViewModel() {
    private val routineIdArg: String = checkNotNull(savedStateHandle["routineId"])
    private val routineId = if (routineIdArg == SuperPlannerRoutes.NEW_ROUTINE_ID) RoutineId(UUID.randomUUID().toString()) else RoutineId(routineIdArg)
    private val _state = MutableStateFlow(RoutineFormUiState())
    val state: StateFlow<RoutineFormUiState> = _state.asStateFlow()

    init {
        if (routineIdArg != SuperPlannerRoutes.NEW_ROUTINE_ID) {
            viewModelScope.launch {
                val existing = getRoutine(routineId) ?: return@launch
                _state.value = RoutineFormUiState(
                    isNew = false,
                    title = existing.title,
                    priority = existing.priority,
                    days = existing.daysOfWeek,
                    hasStartTime = existing.startTime != null,
                    startTime = existing.startTime ?: LocalTime.of(7, 0),
                    steps = existing.steps.sortedBy { it.order }.map {
                        RoutineStepFormState(it.id.value, it.title, it.plannedDuration.toMinutes().toString())
                    },
                )
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, error = null) }
    fun onPriority(value: Priority) = _state.update { it.copy(priority = value) }
    fun toggleDay(day: DayOfWeek) = _state.update { current ->
        val days = current.days.toMutableSet()
        if (!days.add(day)) days.remove(day)
        current.copy(days = days)
    }
    fun setHasStartTime(value: Boolean) = _state.update { it.copy(hasStartTime = value) }
    fun onStartTime(value: LocalTime) = _state.update { it.copy(startTime = value) }

    fun addStep() = _state.update { it.copy(steps = it.steps + RoutineStepFormState()) }
    fun removeStep(index: Int) = _state.update { current -> current.copy(steps = current.steps.filterIndexed { i, _ -> i != index }) }
    fun moveStep(index: Int, direction: Int) = _state.update { current ->
        val target = index + direction
        if (target !in current.steps.indices) return@update current
        val steps = current.steps.toMutableList()
        val item = steps.removeAt(index)
        steps.add(target, item)
        current.copy(steps = steps)
    }
    fun updateStepTitle(index: Int, value: String) = _state.update { current -> current.copy(steps = current.steps.mapIndexed { i, step -> if (i == index) step.copy(title = value) else step }, error = null) }
    fun updateStepDuration(index: Int, value: String) = _state.update { current -> current.copy(steps = current.steps.mapIndexed { i, step -> if (i == index) step.copy(durationMinutes = value.filter(Char::isDigit)) else step }, error = null) }

    fun save() {
        val current = _state.value
        if (current.title.isBlank()) return _state.update { it.copy(error = RoutineFormError.BLANK_TITLE) }
        if (current.steps.isEmpty()) return _state.update { it.copy(error = RoutineFormError.NO_STEPS) }
        val steps = current.steps.mapIndexed { index, step ->
            val minutes = step.durationMinutes.toLongOrNull()
            if (step.title.isBlank() || minutes == null || minutes <= 0) return _state.update { it.copy(error = RoutineFormError.INVALID_STEP) }
            RoutineStep(RoutineStepId(step.id), step.title.trim(), Duration.ofMinutes(minutes), index)
        }
        viewModelScope.launch {
            saveRoutine(Routine(routineId, current.title.trim(), steps, if (current.hasStartTime) current.startTime else null, current.days, current.priority))
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() = viewModelScope.launch {
        deleteRoutine(routineId)
        _state.update { it.copy(finished = true) }
    }
}
