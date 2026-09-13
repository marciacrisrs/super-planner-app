package com.superplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val plannedDurationMinutes: Long,
    val priority: String,
    val dueEpochMilli: Long?,
    val completedAtEpochMilli: Long?,
)
