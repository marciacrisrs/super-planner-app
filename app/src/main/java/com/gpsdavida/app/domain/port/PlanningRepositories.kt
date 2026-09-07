package com.gpsdavida.app.domain.port

import com.gpsdavida.app.domain.model.Dependency
import com.gpsdavida.app.domain.model.DependencyId
import com.gpsdavida.app.domain.model.Goal
import com.gpsdavida.app.domain.model.GoalId
import com.gpsdavida.app.domain.model.InboxItem
import com.gpsdavida.app.domain.model.InboxItemId
import com.gpsdavida.app.domain.model.Project
import com.gpsdavida.app.domain.model.ProjectId
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeAll(): Flow<List<Goal>>
    suspend fun getById(id: GoalId): Goal?
    suspend fun save(goal: Goal)
    suspend fun delete(id: GoalId)
}

interface ProjectRepository {
    fun observeAll(): Flow<List<Project>>
    suspend fun getById(id: ProjectId): Project?
    suspend fun save(project: Project)
    suspend fun delete(id: ProjectId)
}

interface InboxRepository {
    fun observeAll(): Flow<List<InboxItem>>
    suspend fun getById(id: InboxItemId): InboxItem?
    suspend fun save(item: InboxItem)
    suspend fun delete(id: InboxItemId)
}

interface DependencyRepository {
    fun observeAll(): Flow<List<Dependency>>
    suspend fun save(dependency: Dependency)
    suspend fun delete(id: DependencyId)
}
