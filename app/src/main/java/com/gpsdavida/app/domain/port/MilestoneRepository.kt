package com.gpsdavida.app.domain.port

import com.gpsdavida.app.domain.model.Milestone
import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {
    fun observeAll(): Flow<List<Milestone>>
    suspend fun save(milestone: Milestone)
    suspend fun delete(id: String)
}
