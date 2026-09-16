package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.RoutineEntity
import com.superplanner.app.data.local.RoutineStepEntity
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.RoutineId
import com.superplanner.app.domain.model.RoutineStep
import com.superplanner.app.domain.model.RoutineStepId
import java.time.DayOfWeek
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

fun RoutineEntity.toDomain(steps: List<RoutineStepEntity>): Routine = Routine(
    id = RoutineId(id),
    title = title,
    steps = steps.map { it.toDomain() },
    startTime = startTimeMinute?.let { LocalTime.of(it / MINUTES_PER_HOUR, it % MINUTES_PER_HOUR) },
    daysOfWeek = decodeDays(daysOfWeek),
    priority = Priority.valueOf(priority),
)

fun RoutineStepEntity.toDomain(): RoutineStep = RoutineStep(
    id = RoutineStepId(id),
    title = title,
    plannedDuration = java.time.Duration.ofMinutes(plannedDurationMinutes),
    order = stepOrder,
)

fun Routine.toEntity(): RoutineEntity = RoutineEntity(
    id = id.value,
    title = title,
    startTimeMinute = startTime?.let { it.hour * MINUTES_PER_HOUR + it.minute },
    daysOfWeek = daysOfWeek.joinToString(",") { it.name },
    priority = priority.name,
)

fun Routine.toStepEntities(): List<RoutineStepEntity> = steps.map { step ->
    RoutineStepEntity(
        id = step.id.value,
        routineId = id.value,
        title = step.title,
        plannedDurationMinutes = step.plannedDuration.toMinutes(),
        stepOrder = step.order,
    )
}

private fun decodeDays(value: String): Set<DayOfWeek> =
    value.split(',').filter { it.isNotBlank() }.map { DayOfWeek.valueOf(it) }.toSet()
