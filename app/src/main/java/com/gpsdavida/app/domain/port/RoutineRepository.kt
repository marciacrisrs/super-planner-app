package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.RoutineId
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeAll(): Flow<List<Routine>>
    suspend fun getById(id: RoutineId): Routine?
    suspend fun save(routine: Routine)
    suspend fun delete(id: RoutineId)
}
