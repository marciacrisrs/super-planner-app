package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.PreferenceSource
import com.superplanner.app.domain.model.PreferenceStatus
import com.superplanner.app.domain.model.PreferenceValue
import com.superplanner.app.domain.model.UserPreference
import com.superplanner.app.domain.model.UserPreferenceId
import com.superplanner.app.domain.port.UserPreferenceRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ManageUserPreferenceTest {
    private val created = Instant.parse("2026-09-13T12:00:00Z")
    private val updated = Instant.parse("2026-09-13T13:00:00Z")

    @Test
    fun `ai suggestion stays separate until user confirms it`() = runBlocking {
        val repository = InMemoryPreferenceRepository()
        val preference = suggestedPreference()
        repository.save(preference)

        assertEquals(PreferenceStatus.SUGGESTED, repository.getById(preference.id)?.status)
        assertTrue(repository.observeActive().map { it.isEmpty() }.firstValue())

        ConfirmUserPreference(repository)(preference.id, updated)

        assertEquals(PreferenceStatus.CONFIRMED, repository.getById(preference.id)?.status)
        assertEquals(PreferenceSource.USER, repository.getById(preference.id)?.source)
    }

    @Test
    fun `revoking a preference removes it from active memory without deleting execution history`() = runBlocking {
        val repository = InMemoryPreferenceRepository()
        val preference = suggestedPreference().confirm(updated)
        repository.save(preference)
        repository.executionHistorySize = 4

        RevokeUserPreference(repository)(preference.id, Instant.parse("2026-09-14T10:00:00Z"))

        assertEquals(PreferenceStatus.REVOKED, repository.getById(preference.id)?.status)
        assertTrue(repository.observeActive().map { it.isEmpty() }.firstValue())
        assertEquals(4, repository.executionHistorySize)
    }

    @Test
    fun `ai suggestions require evidence and do not persist by themselves`() {
        val suggestion = com.superplanner.app.domain.model.PreferenceSuggestion(
            id = UserPreferenceId("morning-work"),
            title = "Prefere trabalho pela manhã",
            explanation = "As execuções observadas terminam melhor nesse período.",
            value = PreferenceValue.PreferredTime(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(12, 0)),
            basedOnObservationCount = 2,
        )

        assertNull(SuggestUserPreference()(suggestion))
        assertEquals(suggestion, SuggestUserPreference()(suggestion, minimumObservations = 2))
    }

    private fun suggestedPreference() = UserPreference(
        id = UserPreferenceId("morning-work"),
        title = "Prefere trabalho pela manhã",
        value = PreferenceValue.PreferredTime(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(12, 0)),
        source = PreferenceSource.AI_SUGGESTION,
        status = PreferenceStatus.SUGGESTED,
        createdAt = created,
        updatedAt = created,
    )

    private class InMemoryPreferenceRepository : UserPreferenceRepository {
        private val state = MutableStateFlow<List<UserPreference>>(emptyList())
        var executionHistorySize: Int = 0

        override fun observeActive(): Flow<List<UserPreference>> = state.map { items ->
            items.filter { it.status == PreferenceStatus.CONFIRMED }
        }

        override suspend fun getById(id: UserPreferenceId): UserPreference? =
            state.value.firstOrNull { it.id == id }

        override suspend fun save(preference: UserPreference) {
            state.value = state.value.filterNot { it.id == preference.id } + preference
        }

        override suspend fun revoke(id: UserPreferenceId) {
            val current = getById(id) ?: return
            state.value = state.value.filterNot { it.id == id } + current.revoke(updated)
        }
    }
}

private suspend fun <T> Flow<T>.firstValue(): T = kotlinx.coroutines.flow.first()
