package com.superplanner.app.data.local

import androidx.room.Entity

@Entity(tableName = "habit_completions", primaryKeys = ["habitId", "epochDay"])
data class HabitCompletionEntity(
    val habitId: String,
    val epochDay: Long,
    val completedAtEpochMilli: Long,
)
