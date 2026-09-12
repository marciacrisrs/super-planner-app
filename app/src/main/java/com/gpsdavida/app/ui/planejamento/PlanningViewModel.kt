package com.superplanner.app.ui.planejamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Goal
import com.superplanner.app.domain.model.GoalId
import com.superplanner.app.domain.model.InboxItem
import com.superplanner.app.domain.model.InboxItemId
import com.superplanner.app.domain.model.InboxStatus
import com.superplanner.app.domain.model.Project
import com.superplanner.app.domain.model.ProjectId
import com.superplanner.app.domain.model.ProjectStep
import com.superplanner.app.domain.model.ProjectStepId
import com.superplanner.app.domain.port.GoalRepository
import com.superplanner.app.domain.port.InboxRepository
import com.superplanner.app.domain.port.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlanningViewModel @Inject constructor(
    private val goals: GoalRepository,
    private val projects: ProjectRepository,
    private val inbox: InboxRepository,
) : ViewModel() {
    data class State(val goals: List<Goal> = emptyList(), val projects: List<Project> = emptyList(), val inbox: List<InboxItem> = emptyList())

    val state: StateFlow<State> = combine(goals.observeAll(), projects.observeAll(), inbox.observeAll()) { g, p, i -> State(g, p, i) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    fun createGoal(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { goals.save(Goal(GoalId(UUID.randomUUID().toString()), title.trim())) }
    }

    fun deleteGoal(id: String) = viewModelScope.launch { goals.delete(GoalId(id)) }

    fun createProject(title: String, goalId: String?) {
        if (title.isBlank()) return
        viewModelScope.launch { projects.save(Project(ProjectId(UUID.randomUUID().toString()), title.trim(), goalId?.let(::GoalId))) }
    }

    fun addProjectStep(project: Project, title: String, minutes: Long) {
        if (title.isBlank() || minutes <= 0) return
        viewModelScope.launch {
            val step = ProjectStep(ProjectStepId(UUID.randomUUID().toString()), title.trim(), Duration.ofMinutes(minutes), project.steps.size)
            projects.save(project.copy(steps = project.steps + step))
        }
    }

    fun deleteProject(id: String) = viewModelScope.launch { projects.delete(ProjectId(id)) }

    fun capture(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { inbox.save(InboxItem(InboxItemId(UUID.randomUUID().toString()), text.trim(), createdAt = System.currentTimeMillis())) }
    }

    fun setInboxStatus(item: InboxItem, status: InboxStatus) = viewModelScope.launch { inbox.save(item.copy(status = status)) }
    fun deleteInbox(id: String) = viewModelScope.launch { inbox.delete(InboxItemId(id)) }
}
