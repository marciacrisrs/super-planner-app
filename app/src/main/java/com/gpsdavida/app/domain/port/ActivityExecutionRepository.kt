package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import kotlinx.coroutines.flow.Flow

interface ActivityExecutionRepository {
    suspend fun save(activity: ActivityInstance)

    suspend fun getById(id: ActivityInstanceId): ActivityExecution?

    fun observeAll(): Flow<List<ActivityExecution>>
}
