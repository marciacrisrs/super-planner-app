package com.superplanner.app.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.usecase.ObserveRoutines
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RoutinesListViewModel @Inject constructor(
    observeRoutines: ObserveRoutines,
) : ViewModel() {
    val routines: StateFlow<List<Routine>> = observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
