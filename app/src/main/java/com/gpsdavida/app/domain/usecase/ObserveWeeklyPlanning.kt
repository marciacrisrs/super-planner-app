package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.WeeklyActivity
import com.superplanner.app.domain.model.WeeklyActivityKind
import com.superplanner.app.domain.model.WeeklyDaySummary
import com.superplanner.app.domain.model.WeeklyPlanning
import com.superplanner.app.domain.port.ActivityExecutionRepository
import com.superplanner.app.domain.port.AvailabilityRepository
import com.superplanner.app.domain.port.EventRepository
import com.superplanner.app.domain.port.HabitRepository
import com.superplanner.app.domain.port.RoutineRepository
import com.superplanner.app.domain.port.TaskRepository
import com.superplanner.app.domain.port.WeeklyPlanOverride
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
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
    private val weeklyOverrides: WeeklyPlanOverrideRepository,
    private val materialize: MaterializeDailyActivities,
    private val generateDailySchedule: GenerateDailySchedule,
    private val clock: Clock,
) {
    operator fun invoke(startDate: LocalDate, zoneId: ZoneId = clock.zone): Flow<WeeklyPlanning> = combine(listOf(
        events.observeAll(), tasks.observeAll(), habits.observeAll(), routines.observeAll(),
        executions.observeAll(), availability.observeAll(), weeklyOverrides.observe(),
    )) { values ->
        buildWeeklyPlanning(
            startDate = startDate,
            zoneId = zoneId,
            eventList = values[0] as List<com.superplanner.app.domain.model.Event>,
            taskList = values[1] as List<com.superplanner.app.domain.model.Task>,
            habitList = values[2] as List<com.superplanner.app.domain.model.Habit>,
            routineList = values[3] as List<com.superplanner.app.domain.model.Routine>,
            executionList = values[4] as List<ActivityExecution>,
            availabilityList = values[5] as List<Availability>,
            overrides = values[6] as List<WeeklyPlanOverride>,
        )
    }

    private fun buildWeeklyPlanning(
        startDate: LocalDate,
        zoneId: ZoneId,
        eventList: List<com.superplanner.app.domain.model.Event>,
        taskList: List<com.superplanner.app.domain.model.Task>,
        habitList: List<com.superplanner.app.domain.model.Habit>,
        routineList: List<com.superplanner.app.domain.model.Routine>,
        executionList: List<ActivityExecution>,
        availabilityList: List<Availability>,
        overrides: List<WeeklyPlanOverride>,
    ): WeeklyPlanning {
        val baseDays = (0L..6L).map { offset ->
            val date = startDate.plusDays(offset)
            val materialized = materialize(
                events = eventList,
                tasks = taskList,
                habits = habitList.map { habit -> com.superplanner.app.domain.model.HabitDay(habit, date, null) },
                routines = routineList,
                date = date,
                zoneId = zoneId,
            )
            val schedule = generateDailySchedule(
                activities = materialized.map { it.instance }, date = date,
                availability = availabilityList, zoneId = zoneId,
            )
            val titlesById = materialized.associateBy { it.instance.id }
            val activities = schedule.activities.mapNotNull { instance ->
                val source = titlesById[instance.id] ?: return@mapNotNull null
                WeeklyActivity(date, source.title, instance.kind(), instance)
            }
            val dayExecutions = executionList.filter { execution ->
                execution.actual?.start?.atZone(zoneId)?.toLocalDate() == date ||
                    execution.planned.start.atZone(zoneId).toLocalDate() == date
            }
            WeeklyDaySummary(
                date = date,
                activities = activities,
                plannedDuration = activities.fold(java.time.Duration.ZERO) { total, item -> total.plus(item.instance.plannedDuration) },
                actualDuration = dayExecutions.fold(java.time.Duration.ZERO) { total, execution ->
                    total.plus(execution.actual?.duration ?: java.time.Duration.ZERO)
                },
                plannedCount = activities.size,
                completedCount = dayExecutions.count { it.status == ActivityStatus.DONE },
                conflictCount = schedule.conflicts.size,
            )
        }

        val weekDates = baseDays.map { it.date }.toSet()
        val byId = baseDays.flatMap { it.activities }.associateBy { it.instance.id.value }
        val overriddenIds = overrides.map { it.activityId }.toSet()
        val relocated = overrides.mapNotNull { override ->
            val activity = byId[override.activityId] ?: return@mapNotNull null
            val targetDate = LocalDate.parse(override.date)
            if (targetDate !in weekDates) return@mapNotNull null
            activity to WeeklyActivity(
                date = targetDate,
                title = activity.title,
                kind = activity.kind,
                instance = activity.instance.copy(
                    planned = com.superplanner.app.domain.model.TimeRange(override.start, override.end),
                ),
            )
        }.associateBy { it.first.instance.id.value }

        val days = baseDays.map { day ->
            val movedOut = day.activities.filter { activity ->
                activity.instance.id.value in overriddenIds &&
                    relocated[activity.instance.id.value]?.second?.date != day.date
            }
            val movedIn = relocated.values
                .map { it.second }
                .filter { it.date == day.date }
            val retained = day.activities.filterNot { activity ->
                movedOut.any { it.instance.id == activity.instance.id }
            }
            val finalActivities = (retained + movedIn)
                .distinctBy { it.instance.id }
                .sortedBy { it.instance.planned.start }
            day.copy(
                activities = finalActivities,
                plannedDuration = finalActivities.fold(java.time.Duration.ZERO) { total, item -> total.plus(item.instance.plannedDuration) },
                plannedCount = finalActivities.size,
            )
        }
        return WeeklyPlanning(startDate, startDate.plusDays(6), days)
    }

    private fun com.superplanner.app.domain.model.ActivityInstance.kind() = when (source) {
        is ActivitySource.FromEvent -> WeeklyActivityKind.EVENT
        is ActivitySource.FromTask -> WeeklyActivityKind.TASK
        is ActivitySource.FromHabit -> WeeklyActivityKind.HABIT
        is ActivitySource.FromRoutineStep -> WeeklyActivityKind.ROUTINE
    }
}
