package com.superplanner.app.ui.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.data.RoomLeisureRepository
import com.superplanner.app.domain.model.ContextNote
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: RoomLeisureRepository,
) : ViewModel() {
    private val targetType = MutableStateFlow("planejamento")
    private val targetId = MutableStateFlow("geral")

    val notes: StateFlow<List<ContextNote>> = combine(targetType, targetId) { type, id -> type to id }
        .flatMapLatest { (type, id) -> repository.observeNotes(type, id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setContext(type: String, id: String) {
        targetType.value = type.ifBlank { "planejamento" }
        targetId.value = id.ifBlank { "geral" }
    }

    fun save(type: String, id: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            setContext(type, id)
            repository.saveNote(ContextNote(UUID.randomUUID().toString(), targetType.value, targetId.value, body.trim(), System.currentTimeMillis()))
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteNote(id) }
    }
}
