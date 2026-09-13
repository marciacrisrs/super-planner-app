package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.port.ActivityExecutionRepository
import com.superplanner.app.domain.port.AvailabilityRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Builds the executable day schedule and overlays persisted execution state. */
class ObserveExecutableDay @Inject constructor(
    private val observeEventsForDay: ObserveEventsForDay,
    private val observeTasksForDay: ObserveTasksForDay,
    private val observeHabitDays: ObserveHabitDays,
    private val observeRoutines: ObserveRoutines,
    private val availabilityRepository: AvailabilityRepository,
    private val materializeDailyActivities: MaterializeDailyActivities,
    private val generateDailySchedule: GenerateDailySchedule,
    private val applyPersistedExecutions: ApplyPersistedExecutions,
    private val executions: ActivityExecutionRepository,
    private val clock: Clock,
) {
    operator fun invoke(date: LocalDate = LocalDate.now(clock)): Flow<List<DailyActivity>> {
        val zoneId = clock.zone
        return combine(
            combine(
                observeEventsForDay(date),
                observeTasksForDay(date),
                observeHabitDays(date),
            ) { events, tasks, habits -> Triple(events, tasks, habits) },
            combine(
                observeRoutines(),
                availabilityRepository.observeForDay(date.dayOfWeek),
                executions.observeAll(),
            ) { routines, availability, persisted -> Triple(routines, availability, persisted) },
        ) { catalog, scheduleInputs ->
            val (events, tasks, habits) = catalog
            val (routines, availability, persisted) = scheduleInputs
            val materialized = materializeDailyActivities(
                events = events,
                tasks = tasks,
                habits = habits,
                routines = routines,
                date = date,
                zoneId = zoneId,
            )
            val scheduled = generateDailySchedule(
                activities = materialized.map { it.instance },
                date = date,
                availability = availability,
                zoneId = zoneId,
            ).activities
            val titlesById = materialized.associate { it.instance.id to it.title }
            val merged = applyPersistedExecutions(
                activities = scheduled,
                persisted = persisted.associateBy { it.activityInstanceId },
            )
            merged.map { instance ->
                DailyActivity(
                    title = titlesById[instance.id].orEmpty(),
                    instance = instance,
                )
            }
        }
    }
}
