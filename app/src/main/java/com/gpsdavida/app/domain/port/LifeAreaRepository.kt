package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.LifeArea
import kotlinx.coroutines.flow.Flow

interface LifeAreaRepository {
    fun observeAll(): Flow<List<LifeArea>>
    suspend fun save(area: LifeArea)
    suspend fun delete(id: String)
}
