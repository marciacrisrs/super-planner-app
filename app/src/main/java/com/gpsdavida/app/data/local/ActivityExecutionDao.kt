package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityExecutionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: ActivityExecutionEntity): Long

    @Query("UPDATE activity_executions SET status = 'IN_PROGRESS', actualStart = :actualStart, actualEnd = NULL WHERE activityInstanceId = :id AND status = 'PENDING'")
    suspend fun markStartedIfPending(id: String, actualStart: String): Int

    @Query("UPDATE activity_executions SET status = 'DONE', actualEnd = :actualEnd WHERE activityInstanceId = :id AND status = 'IN_PROGRESS' AND actualStart IS NOT NULL AND actualStart <= :actualEnd")
    suspend fun markCompletedIfInProgress(id: String, actualEnd: String): Int

    @Upsert
    suspend fun upsert(entity: ActivityExecutionEntity)

    @Query("SELECT * FROM activity_executions WHERE activityInstanceId = :id LIMIT 1")
    suspend fun getById(id: String): ActivityExecutionEntity?

    @Query("SELECT * FROM activity_executions")
    fun observeAll(): Flow<List<ActivityExecutionEntity>>

    @Transaction
    suspend fun startIfPending(entity: ActivityExecutionEntity): Boolean {
        insertIfAbsent(entity.copy(status = "PENDING", actualStart = null, actualEnd = null))
        return markStartedIfPending(entity.activityInstanceId, checkNotNull(entity.actualStart)) == 1
    }
}