package com.superplanner.app.data

import com.superplanner.app.data.local.ActivityExecutionDao
import com.superplanner.app.data.mapper.toDomain
import com.superplanner.app.data.mapper.toExecutionEntity
import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.port.ActivityExecutionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActivityExecutionRepository @Inject constructor(
    private val dao: ActivityExecutionDao,
) : ActivityExecutionRepository {
    override suspend fun save(activity: ActivityInstance) {
        dao.upsert(activity.toExecutionEntity())
    }

    override suspend fun getById(id: ActivityInstanceId): ActivityExecution? =
        dao.getById(id.value)?.toDomain()

    override fun observeAll(): Flow<List<ActivityExecution>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }
}
