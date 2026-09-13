package com.superplanner.app.data.local

import androidx.room.Entity

@Entity(tableName = "availabilities", primaryKeys = ["id"])
data class AvailabilityEntity(
    val id: String,
    val dayOfWeek: Int,
    val startMinute: Int,
    val endMinute: Int,
    val kind: String,
)