package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): EventEntity?

    @Upsert
    suspend fun upsert(entity: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun delete(id: String)
}
