package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoutineDao {
    @Query("SELECT * FROM routines ORDER BY title")
    abstract fun observeAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_steps ORDER BY routineId, stepOrder")
    abstract fun observeAllSteps(): Flow<List<RoutineStepEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    abstract suspend fun getById(id: String): RoutineEntity?

    @Query("SELECT * FROM routine_steps WHERE routineId = :routineId ORDER BY stepOrder")
    abstract suspend fun getSteps(routineId: String): List<RoutineStepEntity>

    @Upsert
    abstract suspend fun upsertRoutine(entity: RoutineEntity)

    @Upsert
    abstract suspend fun upsertSteps(entities: List<RoutineStepEntity>)

    @Query("DELETE FROM routine_steps WHERE routineId = :routineId")
    abstract suspend fun deleteSteps(routineId: String)

    @Query("DELETE FROM routines WHERE id = :id")
    abstract suspend fun deleteRoutine(id: String)

    @Transaction
    open suspend fun replaceRoutine(entity: RoutineEntity, steps: List<RoutineStepEntity>) {
        upsertRoutine(entity)
        deleteSteps(entity.id)
        if (steps.isNotEmpty()) upsertSteps(steps)
    }

    @Transaction
    open suspend fun delete(id: String) {
        deleteSteps(id)
        deleteRoutine(id)
    }
}
