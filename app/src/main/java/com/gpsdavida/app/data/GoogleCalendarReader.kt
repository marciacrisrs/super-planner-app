package com.superplanner.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.superplanner.app.data.local.EventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Reads Google calendars already synchronized to the Android Calendar provider. */
class GoogleCalendarReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun readUpcoming(): List<EventEntity> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val now = System.currentTimeMillis()
        val from = now - 30L * 24 * 60 * 60 * 1000
        val to = now + 90L * 24 * 60 * 60 * 1000
        val calendarIds = googleCalendarIds()
        if (calendarIds.isEmpty()) return@withContext emptyList()

        val result = mutableListOf<EventEntity>()
        calendarIds.forEach { calendarId ->
            val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendPath(from.toString())
                .appendPath(to.toString())
                .build()
            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
            )
            context.contentResolver.query(
                uri,
                projection,
                "${CalendarContract.Instances.CALENDAR_ID} = ?",
                arrayOf(calendarId.toString()),
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                val eventIdIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                while (cursor.moveToNext()) {
                    val eventId = cursor.getLong(eventIdIndex)
                    val begin = cursor.getLong(beginIndex)
                    val end = cursor.getLong(endIndex)
                    val title = cursor.getString(titleIndex).orEmpty().ifBlank { "Evento sem título" }
                    if (end <= begin) continue
                    result += EventEntity(
                        id = "google-calendar:$calendarId:$eventId:$begin",
                        title = title,
                        startEpochMilli = begin,
                        endEpochMilli = end,
                        recurrenceDays = "",
                        recurrenceInterval = 1,
                        recurrenceUnit = null,
                        recurrenceEndEpochDay = null,
                        priority = "REQUIRED",
                    )
                }
            }
        }
        result.distinctBy { it.id }
    }

    private fun googleCalendarIds(): List<Long> {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val result = mutableListOf<Long>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.ACCOUNT_TYPE} = ? AND ${CalendarContract.Calendars.VISIBLE} = 1",
            arrayOf("com.google"),
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            while (cursor.moveToNext()) result += cursor.getLong(idIndex)
        }
        return result
    }
}
