package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.HabitEntity
import com.superplanner.app.domain.model.GoalId
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.model.LocalTimeWindow
import com.superplanner.app.domain.model.Priority
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

fun HabitEntity.toDomain(): Habit =
    Habit(
        id = HabitId(id),
        title = title,
        plannedDuration = Duration.ofMinutes(plannedDurationMinutes),
        daysOfWeek = daysOfWeek.toDaySet(),
        window = windowStartMinute?.let { start ->
            windowEndMinute?.let { end ->
                LocalTimeWindow(
                    start = LocalTime.ofSecondOfDay(start * MINUTES_PER_HOUR.toLong()),
                    end = LocalTime.ofSecondOfDay(end * MINUTES_PER_HOUR.toLong()),
                )
            }
        },
        priority = Priority.valueOf(priority),
        goalId = goalId?.let(::GoalId),
    )

fun Habit.toEntity(): HabitEntity =
    HabitEntity(
        id = id.value,
        title = title,
        plannedDurationMinutes = plannedDuration.toMinutes().coerceAtLeast(1),
        daysOfWeek = daysOfWeek.joinToString(",") { it.name },
        windowStartMinute = window?.start?.toSecondOfDay()?.div(MINUTES_PER_HOUR)?.toInt(),
        windowEndMinute = window?.end?.toSecondOfDay()?.div(MINUTES_PER_HOUR)?.toInt(),
        priority = priority.name,
        goalId = goalId?.value,
    )

internal fun String.toDaySet(): Set<DayOfWeek> =
    if (isBlank()) emptySet()
    else split(',').map { DayOfWeek.valueOf(it) }.toSet()
