package com.superplanner.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.DailyReview
import com.superplanner.app.domain.model.WeeklyReview
import com.superplanner.app.domain.usecase.BuildReviews
import com.superplanner.app.domain.usecase.ObserveWeeklyPlanning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DailyReviewViewModel @Inject constructor(
    observe: ObserveWeeklyPlanning,
    buildReviews: BuildReviews,
    clock: Clock,
) : ViewModel() {
    val state: StateFlow<DailyReview> = observe(LocalDate.now(clock).with(DayOfWeek.MONDAY))
        .map { buildReviews.daily(it, LocalDate.now(clock)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyReview(LocalDate.now(clock), null, 0, 0, emptyList(), emptyList(), emptyList()))
}

@HiltViewModel
class WeeklyReviewViewModelV2 @Inject constructor(
    observe: ObserveWeeklyPlanning,
    buildReviews: BuildReviews,
    clock: Clock,
) : ViewModel() {
    val state: StateFlow<WeeklyReview> = observe(LocalDate.now(clock).with(DayOfWeek.MONDAY))
        .map(buildReviews::weekly)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyReview(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
}
