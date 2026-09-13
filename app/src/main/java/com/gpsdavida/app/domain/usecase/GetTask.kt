package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.port.TaskRepository
import javax.inject.Inject

class GetTask @Inject constructor(
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(id: TaskId): Task? = tasks.getById(id)
}
