package com.gpsdavida.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leisure_items")
data class LeisureItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val kind: String,
    val status: String,
    val notes: String?,
)

@Entity(tableName = "reading_goals")
data class ReadingGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val minutesPerSession: Int,
    val sessionsPerWeek: Int,
    val active: Boolean,
)

@Entity(tableName = "context_notes")
data class ContextNoteEntity(
    @PrimaryKey val id: String,
    val targetType: String,
    val targetId: String,
    val body: String,
    val createdAtEpochMilli: Long,
)
