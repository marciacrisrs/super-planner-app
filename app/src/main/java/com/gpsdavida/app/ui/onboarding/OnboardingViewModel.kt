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

data class OnboardingState(
    val finished: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val events: EventRepository,
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val availability: AvailabilityRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun createFirstRoute(
        intent: String,
        wakeTime: String,
        sleepTime: String,
        fixedTitle: String,
        fixedStart: String,
        fixedEnd: String,
        availabilityStart: String,
        availabilityEnd: String,
        recurringActivity: String,
    ) {
        viewModelScope.launch {
            val today = LocalDate.now(clock)
            val zone = clock.zone
            val availableFrom = parseTime(availabilityStart, LocalTime.of(7, 0))
            val availableUntil = parseTime(availabilityEnd, LocalTime.of(21, 0))
            val wake = parseTime(wakeTime, availableFrom)
            val sleep = parseTime(sleepTime, availableUntil)

            if (intent.isNotBlank()) {
                tasks.save(
                    Task(
                        id = TaskId(UUID.randomUUID().toString()),
                        title = intent.trim(),
                        plannedDuration = Duration.ofMinutes(30),
                        priority = Priority.IMPORTANT,
                        due = today.plusDays(1).atStartOfDay(zone).toInstant(),
                    ),
                )
            }

            if (fixedTitle.isNotBlank()) {
                val start = parseTime(fixedStart, LocalTime.of(9, 0))
                val end = parseTime(fixedEnd, start.plusHours(1))
                val safeEnd = if (end.isAfter(start)) end else start.plusHours(1)
                events.save(
                    Event(
                        id = EventId(UUID.randomUUID().toString()),
                        title = fixedTitle.trim(),
                        range = TimeRange(
                            today.atTime(start).atZone(zone).toInstant(),
                            today.atTime(safeEnd).atZone(zone).toInstant(),
                        ),
                    ),
                )
            }

            if (recurringActivity.isNotBlank()) {
                habits.save(
                    Habit(
                        id = HabitId(UUID.randomUUID().toString()),
                        title = recurringActivity.trim(),
                        plannedDuration = Duration.ofMinutes(20),
                        daysOfWeek = setOf(today.dayOfWeek),
                        window = LocalTimeWindow(wake, sleep),
                    ),
                )
            }

            availability.save(
                Availability(
                    id = AvailabilityId(UUID.randomUUID().toString()),
                    dayOfWeek = today.dayOfWeek,
                    window = LocalTimeWindow(availableFrom, availableUntil),
                    kind = AvailabilityKind.FREE,
                ),
            )

            _state.value = OnboardingState(finished = true)
        }
    }

    fun skip() {
        _state.value = OnboardingState(finished = true)
    }

    private fun parseTime(value: String, fallback: LocalTime): LocalTime =
        runCatching { LocalTime.parse(value.trim()) }.getOrDefault(fallback)
}
