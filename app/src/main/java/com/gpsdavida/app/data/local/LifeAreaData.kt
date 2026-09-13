package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "life_areas")
data class LifeAreaEntity(@PrimaryKey val id: String, val name: String, val colorKey: String)

@Dao
interface LifeAreaDao {
    @androidx.room.Query("SELECT * FROM life_areas ORDER BY name") fun observeAll(): Flow<List<LifeAreaEntity>>
    @androidx.room.Upsert suspend fun upsert(entity: LifeAreaEntity)
    @androidx.room.Query("DELETE FROM life_areas WHERE id = :id") suspend fun delete(id: String)
}
