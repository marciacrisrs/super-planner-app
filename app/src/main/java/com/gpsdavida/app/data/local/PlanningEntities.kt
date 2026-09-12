package com.superplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val goalId: String?,
    val steps: String,
    val someday: Boolean,
    val waitingFor: String?,
)

@Entity(tableName = "inbox_items")
data class InboxItemEntity(
    @PrimaryKey val id: String,
    val text: String,
    val status: String,
    val goalId: String?,
    val projectId: String?,
    val createdAt: Long,
)
