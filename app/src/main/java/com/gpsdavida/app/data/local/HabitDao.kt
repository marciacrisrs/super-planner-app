package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Upsert
    suspend fun upsert(entity: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: String)
}
