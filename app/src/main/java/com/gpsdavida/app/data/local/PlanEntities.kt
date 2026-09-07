package com.gpsdavida.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val objective: String,
    val type: String,
    val origin: String?,
    val status: String,
    val validFrom: String?,
    val validUntil: String?,
    val sourceDocument: String?,
    val version: Int,
)

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val title: String,
    val durationMinutes: Int,
    val daysOfWeek: String,
    val time: String?,
    val recurrenceId: String?,
    val notes: String?,
)
