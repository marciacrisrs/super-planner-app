package com.gpsdavida.app.ui.planos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.data.PlanImportInterpreter
import com.gpsdavida.app.data.RoomPlanRepository
import com.gpsdavida.app.domain.model.PlanItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlanImportViewModel @Inject constructor(
    private val interpreter: PlanImportInterpreter,
    private val plans: RoomPlanRepository,
) : ViewModel() {
    data class State(val proposal: PlanImportInterpreter.Proposal? = null, val error: String? = null)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun interpret(uri: Uri) {
        viewModelScope.launch {
            runCatching { interpreter.interpret(uri) }
                .onSuccess { _state.value = State(proposal = it) }
                .onFailure { _state.value = State(error = it.message ?: "Falha ao interpretar") }
        }
    }

    fun cancel() { _state.value = State() }

    fun confirm(planId: String) {
        val proposal = _state.value.proposal ?: return
        viewModelScope.launch {
            proposal.items.forEach { item ->
                plans.saveItem(PlanItem(UUID.randomUUID().toString(), planId, item.title, item.durationMinutes))
            }
            _state.value = State()
        }
    }
}
