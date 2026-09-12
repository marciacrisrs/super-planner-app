package com.superplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_executions")
data class ActivityExecutionEntity(
    @PrimaryKey val activityInstanceId: String,
    val status: String,
    val plannedStart: String,
    val plannedEnd: String,
    val actualStart: String?,
    val actualEnd: String?,
)
