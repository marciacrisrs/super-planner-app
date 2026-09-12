package com.superplanner.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.usecase.ObserveHabits
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HabitsListViewModel @Inject constructor(
    observeHabits: ObserveHabits,
) : ViewModel() {
    val habits: StateFlow<List<Habit>> = observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
