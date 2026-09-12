package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.Task
import com.superplanner.app.domain.model.TaskId
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeAll(): Flow<List<Task>>
    suspend fun getById(id: TaskId): Task?
    suspend fun save(task: Task)
    suspend fun delete(id: TaskId)
}
