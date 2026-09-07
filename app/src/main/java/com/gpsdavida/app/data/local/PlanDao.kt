package com.gpsdavida.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY version DESC, name")
    fun observePlans(): Flow<List<PlanEntity>>
    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlan(id: String): PlanEntity?
    @Upsert suspend fun upsertPlan(plan: PlanEntity)
    @Query("DELETE FROM plans WHERE id = :id") suspend fun deletePlan(id: String)

    @Query("SELECT * FROM plan_items WHERE planId = :planId ORDER BY title")
    fun observeItems(planId: String): Flow<List<PlanItemEntity>>
    @Upsert suspend fun upsertItem(item: PlanItemEntity)
    @Query("DELETE FROM plan_items WHERE id = :id") suspend fun deleteItem(id: String)
}
