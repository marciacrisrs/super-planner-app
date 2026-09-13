package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityExecutionDao {
    @Upsert
    suspend fun upsert(entity: ActivityExecutionEntity)

    @Query("SELECT * FROM activity_executions WHERE activityInstanceId = :id LIMIT 1")
    suspend fun getById(id: String): ActivityExecutionEntity?

    @Query("SELECT * FROM activity_executions")
    fun observeAll(): Flow<List<ActivityExecutionEntity>>
}
