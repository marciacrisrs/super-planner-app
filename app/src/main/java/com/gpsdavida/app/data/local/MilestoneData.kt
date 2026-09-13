package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetEpochDay: Long,
    val goalId: String?,
)

@Dao
interface MilestoneDao {
    @androidx.room.Query("SELECT * FROM milestones ORDER BY targetEpochDay") fun observeAll(): Flow<List<MilestoneEntity>>
    @androidx.room.Upsert suspend fun upsert(entity: MilestoneEntity)
    @androidx.room.Query("DELETE FROM milestones WHERE id = :id") suspend fun delete(id: String)
}
