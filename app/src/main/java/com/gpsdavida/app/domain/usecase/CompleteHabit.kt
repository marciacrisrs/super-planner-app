package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.HabitId
import com.superplanner.app.domain.port.HabitRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class CompleteHabit @Inject constructor(
    private val habits: HabitRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        id: HabitId,
        date: LocalDate = LocalDate.now(clock),
        done: Boolean = true,
    ) {
        habits.setCompleted(id, date, if (done) clock.instant() else null)
    }
}
