package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppMetaDao {
    @Query("SELECT * FROM app_meta WHERE id = 1")
    suspend fun get(): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppMetaEntity)
}
