package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AvailabilityDao {
    @Query("SELECT * FROM availabilities ORDER BY dayOfWeek, startMinute")
    fun observeAll(): Flow<List<AvailabilityEntity>>

    @Query("SELECT * FROM availabilities WHERE dayOfWeek = :day ORDER BY startMinute")
    fun observeForDay(day: Int): Flow<List<AvailabilityEntity>>

    @Upsert
    suspend fun upsert(entity: AvailabilityEntity)

    @Query("DELETE FROM availabilities WHERE id = :id")
    suspend fun delete(id: String)
}