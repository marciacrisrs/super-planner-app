package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceIds
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.DailyActivity
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.HabitDay
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TimeRange
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/** Turns catalog items into daily activity instances before scheduling. */
class MaterializeDailyActivities @Inject constructor() {
    operator fun invoke(
        events: List<Event>,
        tasks: List<Task>,
        habits: List<HabitDay>,
        routines: List<Routine>,
        date: LocalDate,
        zoneId: ZoneId,
    ): List<DailyActivity> = buildList {
        events.forEach { add(it.toDailyActivity(date, zoneId)) }
        tasks.filterNot { it.isDone }.forEach { add(it.toDailyActivity(date, zoneId)) }
        habits.filterNot { it.isDone }.forEach { add(it.toDailyActivity(zoneId)) }
        routines.filter { it.occursOn(date) }.forEach { routine ->
            addAll(routine.toDailyActivities(date, zoneId))
        }
    }

    private fun Event.toDailyActivity(date: LocalDate, zoneId: ZoneId): DailyActivity {
        val start = range.start.atZone(zoneId)
        val end = range.end.atZone(zoneId)
        val planned = TimeRange(
            date.atTime(start.toLocalTime()).atZone(zoneId).toInstant(),
            date.atTime(end.toLocalTime()).atZone(zoneId).toInstant(),
        )
        return DailyActivity(
            title = title,
            instance = ActivityInstance(
                id = ActivityInstanceIds.forEvent(id, date),
                source = ActivitySource.FromEvent(id),
                flexibility = Flexibility.FIXED,
                planned = planned,
                priority = priority,
                energy = energy,
            ),
        )
    }

    private fun Task.toDailyActivity(date: LocalDate, zoneId: ZoneId): DailyActivity {
        val anchor = due?.atZone(zoneId)?.toLocalTime() ?: LocalTime.of(9, 0)
        val start = date.atTime(anchor).atZone(zoneId).toInstant()
        return DailyActivity(
            title = title,
            instance = ActivityInstance(
                id = ActivityInstanceIds.forTask(id, date),
                source = ActivitySource.FromTask(id),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(start, start.plus(plannedDuration)),
                priority = priority,
                energy = energy,
            ),
        )
    }

    private fun HabitDay.toDailyActivity(zoneId: ZoneId): DailyActivity {
        val anchor = habit.window?.start ?: LocalTime.of(9, 0)
        val start = date.atTime(anchor).atZone(zoneId).toInstant()
        return DailyActivity(
            title = habit.title,
            instance = ActivityInstance(
                id = ActivityInstanceIds.forHabit(habit.id, date),
                source = ActivitySource.FromHabit(habit.id),
                flexibility = Flexibility.FLEXIBLE,
                planned = TimeRange(start, start.plus(habit.plannedDuration)),
                priority = habit.priority,
                energy = habit.energy,
            ),
        )
    }

    private fun Routine.occursOn(date: LocalDate): Boolean =
        daysOfWeek.isEmpty() || date.dayOfWeek in daysOfWeek

    private fun Routine.toDailyActivities(date: LocalDate, zoneId: ZoneId): List<DailyActivity> {
        var cursor = startTime?.let { date.atTime(it).atZone(zoneId).toInstant() }
        return steps.sortedBy { it.order }.map { step ->
            val start = cursor ?: date.atTime(LocalTime.of(9, 0)).atZone(zoneId).toInstant()
            val end = start.plus(step.plannedDuration)
            cursor = end
            DailyActivity(
                title = "$title · ${step.title}",
                instance = ActivityInstance(
                    id = ActivityInstanceIds.forRoutineStep(id, step.id, date),
                    source = ActivitySource.FromRoutineStep(id, step.id),
                    flexibility = Flexibility.FLEXIBLE,
                    planned = TimeRange(start, end),
                    priority = priority,
                    energy = energy,
                ),
            )
        }
    }
}
