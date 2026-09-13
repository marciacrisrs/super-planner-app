package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LeisureDao {
    @Query("SELECT * FROM leisure_items ORDER BY kind, title") fun observeItems(): Flow<List<LeisureItemEntity>>
    @Upsert suspend fun upsertItem(item: LeisureItemEntity)
    @Query("DELETE FROM leisure_items WHERE id = :id") suspend fun deleteItem(id: String)

    @Query("SELECT * FROM reading_goals WHERE id = :id") suspend fun getReadingGoal(id: String): ReadingGoalEntity?
    @Query("SELECT * FROM reading_goals ORDER BY id") fun observeReadingGoals(): Flow<List<ReadingGoalEntity>>
    @Upsert suspend fun upsertReadingGoal(goal: ReadingGoalEntity)

    @Query("SELECT * FROM context_notes WHERE targetType = :targetType AND targetId = :targetId ORDER BY createdAtEpochMilli DESC")
    fun observeNotes(targetType: String, targetId: String): Flow<List<ContextNoteEntity>>
    @Upsert suspend fun upsertNote(note: ContextNoteEntity)
    @Query("DELETE FROM context_notes WHERE id = :id") suspend fun deleteNote(id: String)
}
