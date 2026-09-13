package com.superplanner.app.domain.model

import java.time.Duration

data class ProjectStep(
    val id: ProjectStepId,
    val title: String,
    val plannedDuration: Duration,
    val order: Int,
    val completed: Boolean = false,
)

data class Project(
    val id: ProjectId,
    val title: String,
    val goalId: GoalId? = null,
    val steps: List<ProjectStep> = emptyList(),
    val someday: Boolean = false,
    val waitingFor: String? = null,
)

enum class InboxStatus { INBOX, SOMEDAY, WAITING, PROCESSED, DISCARDED }

data class InboxItem(
    val id: InboxItemId,
    val text: String,
    val status: InboxStatus = InboxStatus.INBOX,
    val goalId: GoalId? = null,
    val projectId: ProjectId? = null,
    val createdAt: Long = 0L,
)
