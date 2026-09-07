package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.domain.model.ActivityExecution
import com.gpsdavida.app.domain.model.ActivityStatus
import com.gpsdavida.app.domain.model.ActivitySource
import com.gpsdavida.app.domain.model.Availability
import com.gpsdavida.app.domain.model.WeeklyActivity
import com.gpsdavida.app.domain.model.WeeklyActivityKind
import com.gpsdavida.app.domain.model.WeeklyDaySummary
import com.gpsdavida.app.domain.model.WeeklyPlanning
import com.gpsdavida.app.domain.port.ActivityExecutionRepository
import com.gpsdavida.app.domain.port.AvailabilityRepository
import com.gpsdavida.app.domain.port.EventRepository
import com.gpsdavida.app.domain.port.HabitRepository
import com.gpsdavida.app.domain.port.RoutineRepository
import com.gpsdavida.app.domain.port.TaskRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Provides the application/domain contract consumed by the weekly planner UI. */
class ObserveWeeklyPlanning @Inject constructor(
    private val events: EventRepository,
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val routines: RoutineRepository,
    private val executions: ActivityExecutionRepository,
    private val availability: AvailabilityRepository,
    private val materialize: MaterializeDailyActivities,
    private val generateDailySchedule: GenerateDailySchedule,
    private val clock: Clock,
) {
    operator fun invoke(
        startDate: LocalDate,
        zoneId: ZoneId = clock.zone,
    ): Flow<WeeklyPlanning> = combine(
        events.observeAll(),
        tasks.observeAll(),
        habits.observeAll(),
        routines.observeAll(),
        executions.observeAll(),
        availability.observeAll(),
    ) { eventList, taskList, habitList, routineList, executionList, availabilityList ->
        buildWeeklyPlanning(
            startDate = startDate,
            zoneId = zoneId,
            eventList = eventList,
            taskList = taskList,
            habitList = habitList,
            routineList = routineList,
            executionList = executionList,
            availabilityList = availabilityList,
        )
    }

    private fun buildWeeklyPlanning(
        startDate: LocalDate,
        zoneId: ZoneId,
        eventList: List<com.gpsdavida.app.domain.model.Event>,
        taskList: List<com.gpsdavida.app.domain.model.Task>,
        habitList: List<com.gpsdavida.app.domain.model.Habit>,
        routineList: List<com.gpsdavida.app.domain.model.Routine>,
        executionList: List<ActivityExecution>,
        availabilityList: List<Availability>,
    ): WeeklyPlanning {
        val days = (0L..6L).map { offset ->
            val date = startDate.plusDays(offset)
            val materialized = materialize(
                events = eventList,
                tasks = taskList,
                habits = habitList.map { habit ->
                    com.gpsdavida.app.domain.model.HabitDay(
                        habit = habit,
                        date = date,
                        completedAt = null,
                    )
                },
                routines = routineList,
                date = date,
                zoneId = zoneId,
            )
            val schedule = generateDailySchedule(
                activities = materialized.map { it.instance },
                date = date,
                availability = availabilityList,
                zoneId = zoneId,
            )
            val titlesById = materialized.associateBy { it.instance.id }
            val weeklyActivities = schedule.activities.mapNotNull { instance ->
                val source = titlesById[instance.id] ?: return@mapNotNull null
                WeeklyActivity(
                    date = date,
                    title = source.title,
                    kind = instance.kind(),
                    instance = instance,
                    goalId = source.goalId(),
                )
            }
            val dayExecutions = executionList.filter { execution ->
                execution.actual?.start?.atZone(zoneId)?.toLocalDate() == date ||
                    execution.planned.start.atZone(zoneId).toLocalDate() == date
            }
            WeeklyDaySummary(
                date = date,
                activities = weeklyActivities,
                plannedDuration = weeklyActivities.fold(java.time.Duration.ZERO) { total, item -> total.plus(item.instance.plannedDuration) },
                actualDuration = dayExecutions.fold(java.time.Duration.ZERO) { total, execution ->
                    total.plus(execution.actual?.duration ?: java.time.Duration.ZERO)
                },
                plannedCount = weeklyActivities.size,
                completedCount = dayExecutions.count { it.status == ActivityStatus.DONE },
                conflictCount = schedule.conflicts.size,
            )
        }
        return WeeklyPlanning(startDate, startDate.plusDays(6), days)
    }

    private fun com.gpsdavida.app.domain.model.DailyActivity.goalId() = when (val source = instance.source) {
        is ActivitySource.FromEvent -> null
        is ActivitySource.FromTask -> null
        is ActivitySource.FromHabit -> null
        is ActivitySource.FromRoutineStep -> null
    }

    private fun com.gpsdavida.app.domain.model.ActivityInstance.kind() = when (source) {
        is ActivitySource.FromEvent -> WeeklyActivityKind.EVENT
        is ActivitySource.FromTask -> WeeklyActivityKind.TASK
        is ActivitySource.FromHabit -> WeeklyActivityKind.HABIT
        is ActivitySource.FromRoutineStep -> WeeklyActivityKind.ROUTINE
    }
}
