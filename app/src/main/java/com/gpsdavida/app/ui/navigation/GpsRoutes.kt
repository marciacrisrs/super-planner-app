package com.gpsdavida.app.ui.navigation

object GpsRoutes {
    const val AGORA = "agora"
    const val MEU_DIA = "meu_dia"
    const val WEEK = "week"
    const val WEEK_DAY = "week_day/{date}"
    const val PLANNING = "planning"
    const val EVENTS = "events"
    const val TASKS = "tasks"
    const val HABITS = "habits"
    const val ROUTINES = "routines"
    const val AVAILABILITY = "availability"
    const val EVENT_EDITOR = "event_editor/{eventId}"
    const val TASK_EDITOR = "task_editor/{taskId}"
    const val HABIT_EDITOR = "habit_editor/{habitId}"
    const val ROUTINE_EDITOR = "routine_editor/{routineId}"
    const val NEW_EVENT_ID = "new"
    const val NEW_TASK_ID = "new"
    const val NEW_HABIT_ID = "new"
    const val NEW_ROUTINE_ID = "new"

    fun eventEditor(eventId: String = NEW_EVENT_ID): String = "event_editor/$eventId"
    fun taskEditor(taskId: String = NEW_TASK_ID): String = "task_editor/$taskId"
    fun habitEditor(habitId: String = NEW_HABIT_ID): String = "habit_editor/$habitId"
    fun routineEditor(routineId: String = NEW_ROUTINE_ID): String = "routine_editor/$routineId"
    fun weekDay(date: String): String = "week_day/$date"
}
