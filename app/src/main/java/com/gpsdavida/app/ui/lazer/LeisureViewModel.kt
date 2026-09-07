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
import com.gpsdavida.app.domain.model.Priority
import com.gpsdavida.app.domain.port.HabitRepository
import com.gpsdavida.app.domain.port.TaskRepository
import com.gpsdavida.app.domain.model.Task
import com.gpsdavida.app.domain.model.TaskId
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
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
    private val tasks: TaskRepository,
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

    fun schedule(item: LeisureItem) {
        viewModelScope.launch {
            tasks.save(
                Task(
                    id = TaskId("leisure-${item.id}"),
                    title = item.title,
                    plannedDuration = Duration.ofMinutes(45),
                    priority = Priority.LEISURE,
                    due = ZonedDateTime.now().plusHours(1).toInstant(),
                ),
            )
        }
    }

    fun delete(id: String) = viewModelScope.launch { leisure.deleteItem(id) }

    fun saveReading(minutes: Int, sessions: Int) {
        if (minutes <= 0 || sessions <= 0) return
        viewModelScope.launch {
            val clamped = sessions.coerceAtMost(7)
            val days = java.time.DayOfWeek.values().take(clamped).toSet()
            leisure.saveReadingGoal(com.gpsdavida.app.domain.model.ReadingGoal("reading", minutesPerSession = minutes, sessionsPerWeek = clamped))
            habits.save(
                Habit(
                    id = HabitId("reading-reading"),
                    title = "Ler",
                    plannedDuration = Duration.ofMinutes(minutes.toLong()),
                    daysOfWeek = days,
                    window = LocalTimeWindow(LocalTime.of(7, 0), LocalTime.of(22, 0)),
                    priority = Priority.IMPORTANT,
                ),
            )
        }
    }
}
