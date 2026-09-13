package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.PreferenceSuggestion
import com.superplanner.app.domain.model.UserPreference
import com.superplanner.app.domain.model.UserPreferenceId
import com.superplanner.app.domain.port.UserPreferenceRepository
import java.time.Instant
import javax.inject.Inject

class ConfirmUserPreference @Inject constructor(
    private val repository: UserPreferenceRepository,
) {
    suspend operator fun invoke(id: UserPreferenceId, at: Instant): UserPreference {
        val preference = requireNotNull(repository.getById(id)) { "Preference not found: ${id.value}" }
        val confirmed = preference.confirm(at)
        repository.save(confirmed)
        return confirmed
    }
}

class RevokeUserPreference @Inject constructor(
    private val repository: UserPreferenceRepository,
) {
    suspend operator fun invoke(id: UserPreferenceId, at: Instant) {
        repository.revoke(id)
    }
}

class RegisterUserPreference @Inject constructor(
    private val repository: UserPreferenceRepository,
) {
    suspend operator fun invoke(preference: UserPreference) {
        require(preference.source == com.superplanner.app.domain.model.PreferenceSource.USER) {
            "Only explicit user preferences can be registered as confirmed memory"
        }
        require(preference.status == com.superplanner.app.domain.model.PreferenceStatus.CONFIRMED) {
            "Only confirmed preferences can be registered"
        }
        repository.save(preference)
    }
}

/** A suggestion is a proposal only; generating it never mutates preference memory. */
class SuggestUserPreference @Inject constructor() {
    operator fun invoke(
        suggestion: PreferenceSuggestion,
        minimumObservations: Int = 3,
    ): PreferenceSuggestion? =
        suggestion.takeIf { it.basedOnObservationCount >= minimumObservations }
}
