package com.superplanner.app.ui.semana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.WeeklyDaySummary
import com.superplanner.app.domain.usecase.ObserveWeeklyPlanning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeekDayViewModel @Inject constructor(
    private val observeWeeklyPlanning: ObserveWeeklyPlanning,
    private val clock: Clock,
) : ViewModel() {
    fun observeDay(date: LocalDate): StateFlow<WeeklyDaySummary?> =
        observeWeeklyPlanning(date.with(java.time.DayOfWeek.MONDAY))
            .map { planning -> planning.days.firstOrNull { it.date == date } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
