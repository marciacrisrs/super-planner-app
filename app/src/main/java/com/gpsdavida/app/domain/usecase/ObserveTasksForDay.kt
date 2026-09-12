package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.port.TaskRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTasksForDay @Inject constructor(
    private val tasks: TaskRepository,
    private val clock: Clock,
) {
    operator fun invoke(date: LocalDate = LocalDate.now(clock)): Flow<List<Task>> {
        val zone = clock.zone
        return tasks.observeAll().map { list ->
            list.filter { it.belongsOnDay(date, zone) }
                .sortedWith(compareBy<Task> { it.isDone }.thenBy { it.due })
        }
    }
}
