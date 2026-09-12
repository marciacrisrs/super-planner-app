package com.superplanner.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.usecase.CompleteTask
import com.superplanner.app.domain.usecase.ObserveTasks
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TasksListViewModel @Inject constructor(
    observeTasks: ObserveTasks,
    private val completeTask: CompleteTask,
) : ViewModel() {
    val tasks: StateFlow<List<Task>> = observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setDone(id: String, done: Boolean) {
        viewModelScope.launch { completeTask(TaskId(id), done) }
    }
}
