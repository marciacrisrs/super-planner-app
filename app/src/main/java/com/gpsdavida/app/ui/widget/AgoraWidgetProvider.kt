package com.superplanner.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.superplanner.app.R
import com.superplanner.app.ui.MainActivity

class AgoraWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, AgoraWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val snapshot = AgoraWidgetSnapshot.read(context)
            val views = RemoteViews(context.packageName, R.layout.widget_agora)
            val openAppIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            views.setOnClickPendingIntent(R.id.widget_agora_root, openAppIntent)
            views.setTextViewText(R.id.widget_agora_label, context.getString(R.string.widget_agora_label))

            if (snapshot.isEmpty) {
                views.setTextViewText(R.id.widget_agora_title, context.getString(R.string.widget_agora_empty))
                views.setTextViewText(R.id.widget_agora_meta, "")
            } else {
                views.setTextViewText(R.id.widget_agora_title, snapshot.title)
                views.setTextViewText(
                    R.id.widget_agora_meta,
                    context.getString(
                        R.string.widget_agora_meta,
                        snapshot.scheduledTime,
                        snapshot.durationMinutes,
                    ),
                )
            }

            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
