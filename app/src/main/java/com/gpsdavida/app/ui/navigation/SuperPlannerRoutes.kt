package com.superplanner.app.ui.navigation

object SuperPlannerRoutes {
    const val AGORA="agora"; const val MEU_DIA="meu_dia"; const val WEEK="week"; const val WEEK_DAY="week_day/{date}"; const val PLANNING="planning"; const val HORIZONS="horizons"; const val REVIEW="review"; const val FINANCE="finance"; const val LIFE_AREAS="life_areas"; const val DAY_CHECKPOINT="day_checkpoint"; const val PLANS="plans"; const val EVENTS="events"; const val TASKS="tasks"; const val HABITS="habits"; const val ROUTINES="routines"; const val AVAILABILITY="availability"; const val EVENT_EDITOR="event_editor/{eventId}"; const val TASK_EDITOR="task_editor/{taskId}"; const val HABIT_EDITOR="habit_editor/{habitId}"; const val ROUTINE_EDITOR="routine_editor/{routineId}"; const val NEW_EVENT_ID="new"; const val NEW_TASK_ID="new"; const val NEW_HABIT_ID="new"; const val NEW_ROUTINE_ID="new"
    fun eventEditor(id:String=NEW_EVENT_ID)="event_editor/$id"; fun taskEditor(id:String=NEW_TASK_ID)="task_editor/$id"; fun habitEditor(id:String=NEW_HABIT_ID)="habit_editor/$id"; fun routineEditor(id:String=NEW_ROUTINE_ID)="routine_editor/$id"; fun weekDay(date:String)="week_day/$date"
}
