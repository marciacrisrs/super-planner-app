package com.superplanner.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.port.AvailabilityRepository
import com.superplanner.app.domain.port.EventRepository
import com.superplanner.app.domain.port.HabitRepository
import com.superplanner.app.domain.port.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val events: EventRepository,
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val availability: AvailabilityRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun createFirstRoute() {
        viewModelScope.launch {
            val zone = clock.zone
            val today = LocalDate.now(clock)
            val start = today.atTime(19, 0).atZone(zone).toInstant()
            events.save(
                Event(
                    id = EventId(UUID.randomUUID().toString()),
                    title = "Planejamento do dia",
                    range = TimeRange(start, start.plus(Duration.ofMinutes(30))),
                ),
            )
            tasks.save(
                Task(
                    id = TaskId(UUID.randomUUID().toString()),
                    title = "Escolher a próxima prioridade",
                    plannedDuration = Duration.ofMinutes(20),
                    priority = Priority.IMPORTANT,
                    due = today.plusDays(1).atStartOfDay(zone).toInstant(),
                ),
            )
            habits.save(
                Habit(
                    id = HabitId(UUID.randomUUID().toString()),
                    title = "Caminhar",
                    plannedDuration = Duration.ofMinutes(20),
                    daysOfWeek = setOf(java.time.DayOfWeek.values()[today.dayOfWeek.ordinal]),
                    window = LocalTimeWindow(LocalTime.of(7, 0), LocalTime.of(21, 0)),
                ),
            )
            availability.save(
                Availability(
                    id = AvailabilityId(UUID.randomUUID().toString()),
                    dayOfWeek = today.dayOfWeek,
                    window = LocalTimeWindow(LocalTime.of(7, 0), LocalTime.of(21, 0)),
                    kind = AvailabilityKind.FREE,
                ),
            )
            _finished.value = true
        }
    }

    fun skip() {
        _finished.value = true
    }
}
