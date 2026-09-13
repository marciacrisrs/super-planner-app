package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Dependency
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dependencies")
data class DependencyEntity(
    @PrimaryKey val id: String,
    val predecessor: String,
    val successor: String,
)

@Dao
interface DependencyDao {
    @androidx.room.Query("SELECT * FROM dependencies") fun observeAll(): Flow<List<DependencyEntity>>
    @androidx.room.Upsert suspend fun upsert(entity: DependencyEntity)
    @androidx.room.Query("DELETE FROM dependencies WHERE id = :id") suspend fun delete(id: String)
}

object DependencyCodec {
    fun encode(source: ActivitySource): String = when (source) {
        is ActivitySource.FromEvent -> "event:${source.id.value}"
        is ActivitySource.FromTask -> "task:${source.id.value}"
        is ActivitySource.FromHabit -> "habit:${source.id.value}"
        is ActivitySource.FromRoutineStep -> "routine:${source.routineId.value}:${source.stepId.value}"
    }

    fun decode(value: String): ActivitySource? {
        val parts = value.split(":")
        return when (parts.firstOrNull()) {
            "event" -> parts.getOrNull(1)?.let { ActivitySource.FromEvent(com.superplanner.app.domain.model.EventId(it)) }
            "task" -> parts.getOrNull(1)?.let { ActivitySource.FromTask(com.superplanner.app.domain.model.TaskId(it)) }
            "habit" -> parts.getOrNull(1)?.let { ActivitySource.FromHabit(com.superplanner.app.domain.model.HabitId(it)) }
            "routine" -> if (parts.size >= 3) ActivitySource.FromRoutineStep(com.superplanner.app.domain.model.RoutineId(parts[1]), com.superplanner.app.domain.model.RoutineStepId(parts[2])) else null
            else -> null
        }
    }
}
