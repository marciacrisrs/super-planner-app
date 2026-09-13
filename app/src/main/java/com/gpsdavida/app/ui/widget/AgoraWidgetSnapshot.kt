package com.superplanner.app.ui.widget

import android.content.Context

/** Small persisted snapshot shared by the Agora screen and the launcher widget. */
data class AgoraWidgetSnapshot(
    val title: String,
    val scheduledTime: String,
    val durationMinutes: Int,
    val isEmpty: Boolean = false,
) {
    companion object {
        private const val PREFS = "super_planner_widget"
        private const val TITLE = "title"
        private const val SCHEDULED_TIME = "scheduled_time"
        private const val DURATION = "duration"
        private const val EMPTY = "empty"

        fun read(context: Context): AgoraWidgetSnapshot {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return AgoraWidgetSnapshot(
                title = prefs.getString(TITLE, "") ?: "",
                scheduledTime = prefs.getString(SCHEDULED_TIME, "") ?: "",
                durationMinutes = prefs.getInt(DURATION, 0),
                isEmpty = prefs.getBoolean(EMPTY, true),
            )
        }

        fun write(context: Context, snapshot: AgoraWidgetSnapshot) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(TITLE, snapshot.title)
                .putString(SCHEDULED_TIME, snapshot.scheduledTime)
                .putInt(DURATION, snapshot.durationMinutes)
                .putBoolean(EMPTY, snapshot.isEmpty)
                .apply()
        }
    }
}
