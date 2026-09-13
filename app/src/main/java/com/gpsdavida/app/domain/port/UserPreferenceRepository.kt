package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.UserPreference
import com.superplanner.app.domain.model.UserPreferenceId
import kotlinx.coroutines.flow.Flow

interface UserPreferenceRepository {
    fun observeActive(): Flow<List<UserPreference>>
    suspend fun getById(id: UserPreferenceId): UserPreference?
    suspend fun save(preference: UserPreference)
    suspend fun revoke(id: UserPreferenceId)
}
