package com.gpsdavida.app.domain.port

import com.gpsdavida.app.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun observeAll(): Flow<List<Plan>>
    suspend fun getById(id: String): Plan?
    suspend fun save(plan: Plan)
    suspend fun delete(id: String)
}
