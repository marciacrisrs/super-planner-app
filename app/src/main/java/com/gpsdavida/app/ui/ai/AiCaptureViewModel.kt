package com.superplanner.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.ai.AiAssistant
import com.superplanner.app.domain.ai.AiConfirmation
import com.superplanner.app.domain.ai.AiExecution
import com.superplanner.app.domain.ai.AiProposal
import com.superplanner.app.domain.ai.AiRequest
import com.superplanner.app.domain.ai.PlannerAiContextBuilder
import com.superplanner.app.domain.usecase.ObserveExecutableDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class AiCaptureViewModel @Inject constructor(
    private val assistant: AiAssistant,
    private val observeExecutableDay: ObserveExecutableDay,
    private val contextBuilder: PlannerAiContextBuilder,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(AiCaptureUiState())
    val state: StateFlow<AiCaptureUiState> = _state.asStateFlow()

    fun submit(message: String) {
        val normalized = message.trim()
        if (normalized.isEmpty()) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val now = clock.instant()
                val activities = observeExecutableDay(
                    now.atZone(clock.zone).toLocalDate(),
                ).first()
                assistant.propose(
                    AiRequest(
                        message = normalized,
                        context = contextBuilder.build(activities, now = now),
                    ),
                )
            }.onSuccess { proposal ->
                _state.value = _state.value.copy(isLoading = false, proposal = proposal)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Não foi possível interpretar agora.",
                )
            }
        }
    }

    fun confirm() {
        val proposal = _state.value.proposal ?: return
        execute(proposal, AiConfirmation.Confirmed)
    }

    fun dismissProposal() {
        _state.value = _state.value.copy(proposal = null, error = null)
    }

    private fun execute(proposal: AiProposal, confirmation: AiConfirmation) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { assistant.execute(proposal, confirmation) }
                .onSuccess { execution ->
                    _state.value = _state.value.copy(isLoading = false, execution = execution, proposal = null)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message ?: "Não foi possível executar.")
                }
        }
    }
}

data class AiCaptureUiState(
    val isLoading: Boolean = false,
    val proposal: AiProposal? = null,
    val execution: AiExecution? = null,
    val error: String? = null,
)
