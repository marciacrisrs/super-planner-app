package com.superplanner.app.domain.model

import java.time.LocalDate

object ActivityInstanceIds {
    fun forEvent(eventId: EventId, date: LocalDate): ActivityInstanceId =
        ActivityInstanceId("event:${eventId.value}:$date")

    fun forTask(taskId: TaskId, date: LocalDate): ActivityInstanceId =
        ActivityInstanceId("task:${taskId.value}:$date")

    fun forHabit(habitId: HabitId, date: LocalDate): ActivityInstanceId =
        ActivityInstanceId("habit:${habitId.value}:$date")

    fun forRoutineStep(routineId: RoutineId, stepId: RoutineStepId, date: LocalDate): ActivityInstanceId =
        ActivityInstanceId("routine:${routineId.value}:${stepId.value}:$date")
}
