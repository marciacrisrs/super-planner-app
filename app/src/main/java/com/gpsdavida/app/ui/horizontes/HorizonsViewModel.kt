package com.superplanner.app.ui.horizontes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.Milestone
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.port.EventRepository
import com.superplanner.app.domain.port.HabitRepository
import com.superplanner.app.domain.port.MilestoneRepository
import com.superplanner.app.domain.port.RoutineRepository
import com.superplanner.app.domain.port.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthSummary(val month: YearMonth, val activityCount: Int, val plannedMinutes: Long, val completedCount: Int) {
    val completionRatio: Int get() = if (activityCount == 0) 0 else completedCount * 100 / activityCount
}

@HiltViewModel
class HorizonsViewModel @Inject constructor(
    events: EventRepository,
    tasks: TaskRepository,
    habits: HabitRepository,
    routines: RoutineRepository,
    private val milestoneRepository: MilestoneRepository,
    private val clock: Clock,
) : ViewModel() {
    data class State(val currentMonth: MonthSummary = MonthSummary(YearMonth.now(), 0, 0, 0), val months: List<MonthSummary> = emptyList(), val milestones: List<Milestone> = emptyList())
    val state: StateFlow<State> = combine(events.observeAll(), tasks.observeAll(), habits.observeAll(), routines.observeAll(), milestoneRepository.observeAll()) { eventList, taskList, habitList, routineList, milestoneList ->
        val now = YearMonth.now(clock)
        val months = (-5L..6L).map { summarize(now.plusMonths(it), eventList, taskList, habitList, routineList) }
        State(months.first { it.month == now }, months, milestoneList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())
    fun addMilestone(title: String, date: LocalDate) { if (title.isNotBlank()) viewModelScope.launch { milestoneRepository.save(Milestone(java.util.UUID.randomUUID().toString(), title.trim(), date)) } }
    fun deleteMilestone(id: String) { viewModelScope.launch { milestoneRepository.delete(id) } }
    private fun summarize(month: YearMonth, events: List<Event>, tasks: List<Task>, habits: List<Habit>, routines: List<Routine>): MonthSummary {
        val zone = clock.zone; val start = month.atDay(1); val end = month.atEndOfMonth()
        val eventItems = events.filter { event -> (start..end).any { event.occursOn(it, zone) } }
        val taskItems = tasks.filter { task -> task.due?.atZone(zone)?.toLocalDate()?.let { it in start..end } == true || task.completedAt?.atZone(zone)?.toLocalDate()?.let { it in start..end } == true }
        val habitItems = habits.filter { habit -> (start..end).any(habit::occursOn) }
        val routineItems = routines.filter { routine -> (start..end).any { routine.daysOfWeek.isEmpty() || it.dayOfWeek in routine.daysOfWeek } }
        val count = eventItems.size + taskItems.size + habitItems.size + routineItems.size
        val minutes = eventItems.sumOf { java.time.Duration.between(it.range.start, it.range.end).toMinutes() } + taskItems.sumOf { it.plannedDuration.toMinutes() } + habitItems.sumOf { it.plannedDuration.toMinutes() * daysActive(it.daysOfWeek, start, end) } + routineItems.sumOf { it.steps.sumOf { s -> s.plannedDuration.toMinutes() } * daysActive(it.daysOfWeek, start, end) }
        return MonthSummary(month, count, minutes, taskItems.count(Task::isDone))
    }
    private fun daysActive(days: Set<java.time.DayOfWeek>, start: LocalDate, end: LocalDate): Long = if (days.isEmpty()) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1 else (start..end).count { it.dayOfWeek in days }.toLong()
}
