package com.superplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startTimeMinute: Int?,
    val daysOfWeek: String,
    val priority: String,
)

@Entity(tableName = "routine_steps", primaryKeys = ["id", "routineId"])
data class RoutineStepEntity(
    val id: String,
    val routineId: String,
    val title: String,
    val plannedDurationMinutes: Long,
    val stepOrder: Int,
)
