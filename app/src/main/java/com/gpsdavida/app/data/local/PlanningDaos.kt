package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY title") fun observeAll(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals WHERE id = :id") suspend fun getById(id: String): GoalEntity?
    @Upsert suspend fun upsert(entity: GoalEntity)
    @Query("DELETE FROM goals WHERE id = :id") suspend fun delete(id: String)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY title") fun observeAll(): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects WHERE id = :id") suspend fun getById(id: String): ProjectEntity?
    @Upsert suspend fun upsert(entity: ProjectEntity)
    @Query("DELETE FROM projects WHERE id = :id") suspend fun delete(id: String)
}

@Dao
interface InboxItemDao {
    @Query("SELECT * FROM inbox_items ORDER BY createdAt DESC") fun observeAll(): Flow<List<InboxItemEntity>>
    @Query("SELECT * FROM inbox_items WHERE id = :id") suspend fun getById(id: String): InboxItemEntity?
    @Upsert suspend fun upsert(entity: InboxItemEntity)
    @Query("DELETE FROM inbox_items WHERE id = :id") suspend fun delete(id: String)
}
