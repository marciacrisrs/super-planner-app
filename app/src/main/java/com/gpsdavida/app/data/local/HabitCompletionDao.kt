package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE epochDay = :epochDay")
    fun observeByDay(epochDay: Long): Flow<List<HabitCompletionEntity>>

    @Upsert
    suspend fun upsert(entity: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun delete(habitId: String, epochDay: Long)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: String)
}
