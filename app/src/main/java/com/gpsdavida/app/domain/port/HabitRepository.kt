package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeAll(): Flow<List<Habit>>
    suspend fun getById(id: HabitId): Habit?
    suspend fun save(habit: Habit)
    suspend fun delete(id: HabitId)
    fun observeCompletions(date: LocalDate): Flow<Map<HabitId, Instant>>
    suspend fun setCompleted(id: HabitId, date: LocalDate, completedAt: Instant?)
}
