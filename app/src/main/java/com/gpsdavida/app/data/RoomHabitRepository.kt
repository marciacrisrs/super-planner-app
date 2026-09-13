package com.superplanner.app.data

import com.superplanner.app.data.local.HabitCompletionDao
import com.superplanner.app.data.local.HabitCompletionEntity
import com.superplanner.app.data.local.HabitDao
import com.superplanner.app.data.mapper.toDomain
import com.superplanner.app.data.mapper.toEntity
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.port.HabitRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
) : HabitRepository {
    override fun observeAll(): Flow<List<Habit>> =
        habitDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: HabitId): Habit? = habitDao.getById(id.value)?.toDomain()

    override suspend fun save(habit: Habit) {
        habitDao.upsert(habit.toEntity())
    }

    override suspend fun delete(id: HabitId) {
        completionDao.deleteForHabit(id.value)
        habitDao.delete(id.value)
    }

    override fun observeCompletions(date: LocalDate): Flow<Map<HabitId, Instant>> =
        completionDao.observeByDay(date.toEpochDay()).map { rows ->
            rows.associate { HabitId(it.habitId) to Instant.ofEpochMilli(it.completedAtEpochMilli) }
        }

    override suspend fun setCompleted(id: HabitId, date: LocalDate, completedAt: Instant?) {
        if (completedAt == null) {
            completionDao.delete(id.value, date.toEpochDay())
        } else {
            completionDao.upsert(
                HabitCompletionEntity(
                    habitId = id.value,
                    epochDay = date.toEpochDay(),
                    completedAtEpochMilli = completedAt.toEpochMilli(),
                ),
            )
        }
    }
}
