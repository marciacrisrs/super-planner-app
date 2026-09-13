package com.superplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startEpochMilli: Long,
    val endEpochMilli: Long,
    val recurrenceDays: String,
    val recurrenceInterval: Int = 1,
    val recurrenceUnit: String? = null,
    val recurrenceEndEpochDay: Long? = null,
    val priority: String,
)
