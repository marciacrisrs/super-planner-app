package com.superplanner.app.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.superplanner.app.domain.model.ActivityInstance

object ActivityNotificationScheduler {
    private const val ACTION = "com.superplanner.app.NOTIFY_ACTIVITY"
    private const val EXTRA_TITLE = "title"

    fun schedule(context: Context, activity: ActivityInstance) {
        if (activity.planned.start.isBefore(java.time.Instant.now())) return
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ActivityNotificationReceiver::class.java).setAction(ACTION).putExtra(EXTRA_TITLE, activity.source.toString())
        val pending = PendingIntent.getBroadcast(context, activity.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, activity.planned.start.toEpochMilli(), pending)
    }
}
