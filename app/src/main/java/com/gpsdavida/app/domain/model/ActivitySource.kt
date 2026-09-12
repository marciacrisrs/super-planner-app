package com.superplanner.app.domain.model

sealed interface ActivitySource {
    data class FromEvent(val id: EventId) : ActivitySource
    data class FromTask(val id: TaskId) : ActivitySource
    data class FromHabit(val id: HabitId) : ActivitySource
    data class FromRoutineStep(val routineId: RoutineId, val stepId: RoutineStepId) : ActivitySource
}
