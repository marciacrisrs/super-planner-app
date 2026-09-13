package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalTime

@JvmInline
value class UserPreferenceId(val value: String)

enum class PreferenceStatus {
    SUGGESTED,
    CONFIRMED,
    REVOKED,
}

enum class PreferenceSource {
    USER,
    AI_SUGGESTION,
}

sealed interface PreferenceValue {
    data class PreferredTime(val start: LocalTime, val end: LocalTime) : PreferenceValue
    data class AvoidTime(val start: LocalTime, val end: LocalTime) : PreferenceValue
    data class TypicalDuration(val duration: Duration) : PreferenceValue
    data class RequiredContext(val context: ExecutionContext) : PreferenceValue
}

data class UserPreference(
    val id: UserPreferenceId,
    val title: String,
    val value: PreferenceValue,
    val source: PreferenceSource,
    val status: PreferenceStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isActive: Boolean
        get() = status == PreferenceStatus.CONFIRMED

    fun confirm(at: Instant): UserPreference = copy(
        source = PreferenceSource.USER,
        status = PreferenceStatus.CONFIRMED,
        updatedAt = at,
    )

    fun revoke(at: Instant): UserPreference = copy(
        status = PreferenceStatus.REVOKED,
        updatedAt = at,
    )
}

data class PreferenceSuggestion(
    val id: UserPreferenceId,
    val title: String,
    val explanation: String,
    val value: PreferenceValue,
    val basedOnObservationCount: Int,
)
