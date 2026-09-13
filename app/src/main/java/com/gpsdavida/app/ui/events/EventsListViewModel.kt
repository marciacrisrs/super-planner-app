package com.superplanner.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.data.GoogleCalendarReader
import com.superplanner.app.data.local.EventDao
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.usecase.ObserveEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class EventsListViewModel @Inject constructor(
    observeEvents: ObserveEvents,
    private val googleCalendarReader: GoogleCalendarReader,
    private val eventDao: EventDao,
) : ViewModel() {
    val events: StateFlow<List<Event>> = observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importedCount = MutableStateFlow<Int?>(null)
    val importedCount: StateFlow<Int?> = _importedCount.asStateFlow()

    fun importFromGoogleCalendar() {
        if (_isImporting.value) return
        viewModelScope.launch {
            _isImporting.value = true
            _importedCount.value = null
            runCatching {
                val rows = googleCalendarReader.readUpcoming()
                for (row in rows) {
                    eventDao.upsert(row)
                }
                rows.size
            }.onSuccess { _importedCount.value = it }
                .onFailure { _importedCount.value = 0 }
            _isImporting.value = false
        }
    }

    fun clearImportFeedback() {
        _importedCount.value = null
    }
}
