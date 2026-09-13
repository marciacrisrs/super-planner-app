package com.superplanner.app.ui.meudia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.HabitDay
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.usecase.CompleteHabit
import com.superplanner.app.domain.usecase.CompleteTask
import com.superplanner.app.domain.usecase.ObserveEventsForDay
import com.superplanner.app.domain.usecase.ObserveHabitDays
import com.superplanner.app.domain.usecase.ObserveTasksForDay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MeuDiaUiState(
    val events: List<Event> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val habits: List<HabitDay> = emptyList(),
)

@HiltViewModel
class MeuDiaViewModel @Inject constructor(
    observeEventsForDay: ObserveEventsForDay,
    observeTasksForDay: ObserveTasksForDay,
    observeHabitDays: ObserveHabitDays,
    private val completeTask: CompleteTask,
    private val completeHabit: CompleteHabit,
) : ViewModel() {
    val state: StateFlow<MeuDiaUiState> = combine(
        observeEventsForDay(),
        observeTasksForDay(),
        observeHabitDays(),
    ) { events, tasks, habits -> MeuDiaUiState(events, tasks, habits) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeuDiaUiState())

    fun setTaskDone(id: String, done: Boolean) {
        viewModelScope.launch { completeTask(TaskId(id), done) }
    }

    fun setHabitDone(id: String, done: Boolean) {
        viewModelScope.launch { completeHabit(HabitId(id), done = done) }
    }
}
