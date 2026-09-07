package com.gpsdavida.app.data.mapper

import com.gpsdavida.app.data.local.HabitEntity
import com.gpsdavida.app.domain.model.Habit
import com.gpsdavida.app.domain.model.HabitId
import com.gpsdavida.app.domain.model.LocalTimeWindow
import com.gpsdavida.app.domain.model.Priority
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime

fun HabitEntity.toDomain(): Habit =
    Habit(
        id = HabitId(id),
        title = title,
        plannedDuration = Duration.ofMinutes(plannedDurationMinutes),
        daysOfWeek = daysOfWeek.toDaySet(),
        window = windowStartMinute?.let { start ->
            windowEndMinute?.let { end ->
                LocalTimeWindow(
                    start = LocalTime.ofSecondOfDay(start * 60L),
                    end = LocalTime.ofSecondOfDay(end * 60L),
                )
            }
        },
        priority = Priority.valueOf(priority),
        goalId = goalId?.let(::com.gpsdavida.app.domain.model.GoalId),
    )

fun Habit.toEntity(): HabitEntity =
    HabitEntity(
        id = id.value,
        title = title,
        plannedDurationMinutes = plannedDuration.toMinutes().coerceAtLeast(1),
        daysOfWeek = daysOfWeek.joinToString(",") { it.name },
        windowStartMinute = window?.start?.toSecondOfDay()?.div(60),
        windowEndMinute = window?.end?.toSecondOfDay()?.div(60),
        priority = priority.name,
        goalId = goalId?.value,
    )

internal fun String.toDaySet(): Set<DayOfWeek> =
    if (isBlank()) emptySet()
    else split(',').map { DayOfWeek.valueOf(it) }.toSet()
