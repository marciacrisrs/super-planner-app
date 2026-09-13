package com.superplanner.app.ui.semana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.WeeklyPlanning
import com.superplanner.app.domain.usecase.ObserveWeeklyPlanning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val observeWeeklyPlanning: ObserveWeeklyPlanning,
    private val clock: Clock,
) : ViewModel() {
    private val startDate = MutableStateFlow(LocalDate.now(clock).with(java.time.DayOfWeek.MONDAY))

    val selectedStartDate: StateFlow<LocalDate> = startDate

    val state: StateFlow<WeeklyPlanning> = startDate
        .flatMapLatest { observeWeeklyPlanning(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            WeeklyPlanning(
                startDate = startDate.value,
                endDate = startDate.value.plusDays(6),
                days = emptyList(),
            ),
        )

    fun previousWeek() {
        startDate.value = startDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        startDate.value = startDate.value.plusWeeks(1)
    }

    fun currentWeek() {
        startDate.value = LocalDate.now(clock).with(java.time.DayOfWeek.MONDAY)
    }
}
