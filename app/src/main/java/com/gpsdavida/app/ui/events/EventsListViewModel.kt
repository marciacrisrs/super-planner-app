package com.superplanner.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.usecase.ObserveEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EventsListViewModel @Inject constructor(
    observeEvents: ObserveEvents,
) : ViewModel() {
    val events: StateFlow<List<Event>> = observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
