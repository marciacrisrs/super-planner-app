package com.gpsdavida.app.ui.horizontes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.domain.model.Event
import com.gpsdavida.app.domain.model.Habit
import com.gpsdavida.app.domain.model.Milestone
import com.gpsdavida.app.domain.model.Routine
import com.gpsdavida.app.domain.model.Task
import com.gpsdavida.app.domain.port.EventRepository
import com.gpsdavida.app.domain.port.HabitRepository
import com.gpsdavida.app.domain.port.MilestoneRepository
import com.gpsdavida.app.domain.port.RoutineRepository
import com.gpsdavida.app.domain.port.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

 data class MonthSummary(
    val month: YearMonth,
    val activityCount: Int,
    val plannedMinutes: Long,
    val completedCount: Int,
 ) {
    val completionRatio: Int
        get() = if (activityCount == 0) 0 else (completedCount * 100 / activityCount)
}

@HiltViewModel
class HorizonsViewModel @Inject constructor(
    events: EventRepository,
    tasks: TaskRepository,
    habits: HabitRepository,
    routines: RoutineRepository,
    milestones: MilestoneRepository,
    private val clock: Clock,
) : ViewModel() {
    data class State(
        val currentMonth: MonthSummary = MonthSummary(YearMonth.now(), 0, 0, 0),
        val months: List<MonthSummary> = emptyList(),
        val milestones: List<Milestone> = emptyList(),
    )

    val state: StateFlow<State> = combine(
        events.observeAll(),
        tasks.observeAll(),
        habits.observeAll(),
        routines.observeAll(),
        milestones.observeAll(),
    ) { eventList, taskList, habitList, routineList, milestoneList ->
        val now = YearMonth.now(clock)
        val months = (-5L..6L).map { now.plusMonths(it).let { month ->
            month to summarize(month, eventList, taskList, habitList, routineList)
        } }.map { it.second }
        State(months.first(), months, milestoneList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    fun addMilestone(title: String, date: LocalDate) {
        if (title.isBlank()) return
        viewModelScope.launchSafe {
            milestones.save(Milestone(java.util.UUID.randomUUID().toString(), title.trim(), date))
        }
    }

    fun deleteMilestone(id: String) {
        viewModelScope.launchSafe { milestones.delete(id) }
    }

    private fun summarize(
        month: YearMonth,
        events: List<Event>,
        tasks: List<Task>,
        habits: List<Habit>,
        routines: List<Routine>,
    ): MonthSummary {
        val zone = clock.zone
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        val eventItems = events.filter { event ->
            (start..end).any { date -> event.occursOn(date, zone) }
        }
        val taskItems = tasks.filter { task ->
            task.due?.atZone(zone)?.toLocalDate()?.let { it in start..end } == true ||
                task.completedAt?.atZone(zone)?.toLocalDate()?.let { it in start..end } == true
        }
        val habitItems = habits.count { habit -> (start..end).any(habit::occursOn) }
        val routineItems = routines.count { routine ->
            (start..end).any { date -> routine.daysOfWeek.isEmpty() || date.dayOfWeek in routine.daysOfWeek }
        }
        val activityCount = eventItems.size + taskItems.size + habitItems + routineItems
        val plannedMinutes = eventItems.sumOf { it.range.duration.toMinutes() } + taskItems.sumOf { it.plannedDuration.toMinutes() } + habits.filter { habit -> (start..end).any(habit::occursOn) }.sumOf { it.plannedDuration.toMinutes() } + routines.filter { routine -> (start..end).any { date -> routine.daysOfWeek.isEmpty() || date.dayOfWeek in routine.daysOfWeek } }.sumOf { routine -> routine.steps.sumOf { it.plannedDuration.toMinutes() } }
        val completedCount = taskItems.count(Task::isDone)
        return MonthSummary(month, activityCount, plannedMinutes, completedCount)
    }

    private inline fun ViewModel.launchSafe(crossinline block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}
