package com.superplanner.app.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.usecase.CompleteTask
import com.superplanner.app.domain.usecase.DeleteTask
import com.superplanner.app.domain.usecase.GetTask
import com.superplanner.app.domain.usecase.SaveTask
import com.superplanner.app.ui.navigation.SuperPlannerRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskFormUiState(
    val isNew: Boolean = true,
    val title: String = "",
    val durationMinutes: String = "30",
    val priority: Priority = Priority.IMPORTANT,
    val due: Instant? = null,
    val done: Boolean = false,
    val error: TaskFormError? = null,
    val finished: Boolean = false,
)

enum class TaskFormError { BLANK_TITLE, INVALID_DURATION }

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clock: Clock,
    private val getTask: GetTask,
    private val saveTask: SaveTask,
    private val deleteTask: DeleteTask,
    private val completeTask: CompleteTask,
) : ViewModel() {

    private val taskIdArg: String = checkNotNull(savedStateHandle["taskId"])
    private val taskId: TaskId =
        if (taskIdArg == SuperPlannerRoutes.NEW_TASK_ID) TaskId(UUID.randomUUID().toString())
        else TaskId(taskIdArg)

    private val _state = MutableStateFlow(TaskFormUiState())
    val state: StateFlow<TaskFormUiState> = _state.asStateFlow()

    init {
        if (taskIdArg != SuperPlannerRoutes.NEW_TASK_ID) {
            viewModelScope.launch {
                val existing = getTask(taskId) ?: return@launch
                _state.value = TaskFormUiState(
                    isNew = false,
                    title = existing.title,
                    durationMinutes = existing.plannedDuration.toMinutes().toString(),
                    priority = existing.priority,
                    due = existing.due,
                    done = existing.isDone,
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

    fun onDueDate(date: LocalDate?) {
        _state.update { current ->
            current.copy(
                due = date?.let {
                    ZonedDateTime.of(it, LocalTime.of(18, 0), clock.zone).toInstant()
                },
            )
        }
    }

    fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(error = TaskFormError.BLANK_TITLE) }
            return
        }
        val minutes = current.durationMinutes.toLongOrNull()
        if (minutes == null || minutes <= 0) {
            _state.update { it.copy(error = TaskFormError.INVALID_DURATION) }
            return
        }
        viewModelScope.launch {
            saveTask(
                Task(
                    id = taskId,
                    title = current.title.trim(),
                    plannedDuration = Duration.ofMinutes(minutes),
                    priority = current.priority,
                    due = current.due,
                    completedAt = if (current.done) clock.instant() else null,
                ),
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteTask(taskId)
            _state.update { it.copy(finished = true) }
        }
    }

    fun setDone(done: Boolean) {
        _state.update { it.copy(done = done) }
        if (taskIdArg != SuperPlannerRoutes.NEW_TASK_ID) {
            viewModelScope.launch { completeTask(taskId, done) }
        }
    }
}
