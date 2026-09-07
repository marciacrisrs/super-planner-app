package com.gpsdavida.app.domain.port

import com.gpsdavida.app.domain.model.LifeArea
import kotlinx.coroutines.flow.Flow

interface LifeAreaRepository {
    fun observeAll(): Flow<List<LifeArea>>
    suspend fun save(area: LifeArea)
    suspend fun delete(id: String)
}
