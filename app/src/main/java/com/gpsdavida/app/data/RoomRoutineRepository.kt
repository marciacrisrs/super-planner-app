package com.superplanner.app.data

import com.superplanner.app.data.local.RoutineDao
import com.superplanner.app.data.mapper.toDomain
import com.superplanner.app.data.mapper.toEntity
import com.superplanner.app.data.mapper.toStepEntities
import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.RoutineId
import com.superplanner.app.domain.port.RoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomRoutineRepository @Inject constructor(
    private val dao: RoutineDao,
) : RoutineRepository {
    override fun observeAll(): Flow<List<Routine>> = combine(
        dao.observeAll(),
        dao.observeAllSteps(),
    ) { rows, steps ->
        val stepsByRoutine = steps.groupBy { it.routineId }
        rows.map { it.toDomain(stepsByRoutine[it.id].orEmpty()) }
    }

    override suspend fun getById(id: RoutineId): Routine? =
        dao.getById(id.value)?.let { it.toDomain(dao.getSteps(id.value)) }

    override suspend fun save(routine: Routine) {
        dao.replaceRoutine(routine.toEntity(), routine.toStepEntities())
    }

    override suspend fun delete(id: RoutineId) {
        dao.delete(id.value)
    }
}
