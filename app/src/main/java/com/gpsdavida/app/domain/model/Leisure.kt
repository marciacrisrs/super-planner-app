package com.superplanner.app.domain.model

enum class LeisureKind { SERIES, BOOK }
enum class LeisureStatus { WANT, NEXT, ACTIVE, PAUSED, COMPLETED }

data class LeisureItem(
    val id: String,
    val title: String,
    val kind: LeisureKind,
    val status: LeisureStatus = LeisureStatus.WANT,
    val notes: String? = null,
)

data class ReadingGoal(
    val id: String,
    val title: String = "Leitura",
    val minutesPerSession: Int = 20,
    val sessionsPerWeek: Int = 7,
    val active: Boolean = true,
)

data class ContextNote(
    val id: String,
    val targetType: String,
    val targetId: String,
    val body: String,
    val createdAtEpochMilli: Long,
)
