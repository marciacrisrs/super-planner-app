package com.gpsdavida.app.ui.lazer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.data.RoomLeisureRepository
import com.gpsdavida.app.domain.model.Habit
import com.gpsdavida.app.domain.model.HabitId
import com.gpsdavida.app.domain.model.LeisureItem
import com.gpsdavida.app.domain.model.LeisureKind
import com.gpsdavida.app.domain.model.LeisureStatus
import com.gpsdavida.app.domain.model.LocalTimeWindow
import com.gpsdavida.app.domain.port.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LeisureViewModel @Inject constructor(
    private val leisure: RoomLeisureRepository,
    private val habits: HabitRepository,
) : ViewModel() {
    data class State(val items: List<LeisureItem> = emptyList(), val readingMinutes: Int = 20, val readingSessions: Int = 7)
    val state: StateFlow<State> = combine(leisure.observeItems(), leisure.observeReadingGoals()) { items, goals ->
        val goal = goals.firstOrNull()
        State(items, goal?.minutesPerSession ?: 20, goal?.sessionsPerWeek ?: 7)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    fun add(title: String, kind: LeisureKind) {
        if (title.isBlank()) return
        viewModelScope.launch { leisure.saveItem(LeisureItem(UUID.randomUUID().toString(), title.trim(), kind)) }
    }

    fun setStatus(item: LeisureItem, status: LeisureStatus) {
        viewModelScope.launch { leisure.saveItem(item.copy(status = status)) }
    }

    fun delete(id: String) = viewModelScope.launch { leisure.deleteItem(id) }

    fun saveReading(minutes: Int, sessions: Int) {
        if (minutes <= 0 || sessions <= 0) return
        viewModelScope.launch {
            val goalId = "reading"
            leisure.saveReadingGoal(com.gpsdavida.app.domain.model.ReadingGoal(goalId, minutesPerSession = minutes, sessionsPerWeek = sessions))
            habits.save(
                Habit(
                    id = HabitId("reading-$goalId"),
                    title = "Ler",
                    plannedDuration = Duration.ofMinutes(minutes.toLong()),
                    daysOfWeek = emptySet(),
                    window = LocalTimeWindow(LocalTime.of(7, 0), LocalTime.of(22, 0)),
                    priority = com.gpsdavida.app.domain.model.Priority.IMPORTANT,
                ),
            )
        }
    }
}
