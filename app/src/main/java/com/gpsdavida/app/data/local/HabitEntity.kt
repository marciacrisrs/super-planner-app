package com.gpsdavida.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val plannedDurationMinutes: Long,
    val daysOfWeek: String,
    val windowStartMinute: Int?,
    val windowEndMinute: Int?,
    val priority: String,
    val goalId: String?,
)
