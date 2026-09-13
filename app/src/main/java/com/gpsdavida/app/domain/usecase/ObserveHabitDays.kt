package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.HabitDay
import com.superplanner.app.domain.port.HabitRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveHabitDays @Inject constructor(
    private val habits: HabitRepository,
    private val clock: Clock,
) {
    operator fun invoke(date: LocalDate = LocalDate.now(clock)): Flow<List<HabitDay>> =
        combine(habits.observeAll(), habits.observeCompletions(date)) { list, completions ->
            list.filter { it.occursOn(date) }
                .map { habit ->
                    HabitDay(
                        habit = habit,
                        date = date,
                        completedAt = completions[habit.id],
                    )
                }
                .sortedBy { it.habit.title }
        }
}
