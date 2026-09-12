package com.superplanner.app.ui.planos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.data.RoomPlanRepository
import com.superplanner.app.domain.model.Plan
import com.superplanner.app.domain.model.PlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val repository: RoomPlanRepository,
) : ViewModel() {
    val plans: StateFlow<List<Plan>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, objective: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.save(Plan(UUID.randomUUID().toString(), name.trim(), objective.trim()))
        }
    }

    fun setStatus(plan: Plan, status: PlanStatus) {
        viewModelScope.launch { repository.save(plan.copy(status = status)) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}
