package com.gpsdavida.app.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gpsdavida.app.R

class ActivityNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(CHANNEL, "Super Planner", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
        val title = intent.getStringExtra("title") ?: "Próxima ação"
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.next_action_notification_title))
            .setContentText(title)
            .setAutoCancel(true)
            .build()
        manager.notify(title.hashCode(), notification)
    }

    companion object { private const val CHANNEL = "super_planner_activity" }
}
