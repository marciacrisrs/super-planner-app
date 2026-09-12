package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.port.TaskRepository
import javax.inject.Inject

class SaveTask @Inject constructor(
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(task: Task) {
        require(task.title.isNotBlank()) { "title" }
        require(!task.plannedDuration.isNegative && !task.plannedDuration.isZero) { "duration" }
        tasks.save(task)
    }
}
