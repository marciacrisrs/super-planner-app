package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.port.HabitRepository
import javax.inject.Inject

class GetHabit @Inject constructor(
    private val habits: HabitRepository,
) {
    suspend operator fun invoke(id: HabitId): Habit? = habits.getById(id)
}
