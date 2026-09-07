package com.gpsdavida.app.data

import com.gpsdavida.app.data.local.GoalDao
import com.gpsdavida.app.data.local.GoalEntity
import com.gpsdavida.app.data.local.InboxItemDao
import com.gpsdavida.app.data.local.InboxItemEntity
import com.gpsdavida.app.data.local.MilestoneDao
import com.gpsdavida.app.data.local.MilestoneEntity
import com.gpsdavida.app.data.local.ProjectDao
import com.gpsdavida.app.data.local.ProjectEntity
import com.gpsdavida.app.domain.model.Goal
import com.gpsdavida.app.domain.model.GoalId
import com.gpsdavida.app.domain.model.InboxItem
import com.gpsdavida.app.domain.model.InboxItemId
import com.gpsdavida.app.domain.model.InboxStatus
import com.gpsdavida.app.domain.model.Milestone
import com.gpsdavida.app.domain.model.Project
import com.gpsdavida.app.domain.model.ProjectId
import com.gpsdavida.app.domain.model.ProjectStep
import com.gpsdavida.app.domain.model.ProjectStepId
import com.gpsdavida.app.domain.port.GoalRepository
import com.gpsdavida.app.domain.port.InboxRepository
import com.gpsdavida.app.domain.port.MilestoneRepository
import com.gpsdavida.app.domain.port.ProjectRepository
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomGoalRepository @Inject constructor(private val dao: GoalDao) : GoalRepository {
    override fun observeAll(): Flow<List<Goal>> = dao.observeAll().map { it.map { row -> Goal(GoalId(row.id), row.title) } }
    override suspend fun getById(id: GoalId): Goal? = dao.getById(id.value)?.let { Goal(GoalId(it.id), it.title) }
    override suspend fun save(goal: Goal) = dao.upsert(GoalEntity(goal.id.value, goal.title))
    override suspend fun delete(id: GoalId) = dao.delete(id.value)
}

class RoomProjectRepository @Inject constructor(private val dao: ProjectDao) : ProjectRepository {
    override fun observeAll(): Flow<List<Project>> = dao.observeAll().map { it.map(::toDomain) }
    override suspend fun getById(id: ProjectId): Project? = dao.getById(id.value)?.let(::toDomain)
    override suspend fun save(project: Project) = dao.upsert(toEntity(project))
    override suspend fun delete(id: ProjectId) = dao.delete(id.value)
    private fun toEntity(project: Project) = ProjectEntity(project.id.value, project.title, project.goalId?.value, project.steps.sortedBy { it.order }.joinToString("\n") { "${it.id.value}\t${it.order}\t${it.completed}\t${it.plannedDuration.toMinutes()}\t${it.title.replace("\t", " ").replace("\n", " ")}" }, project.someday, project.waitingFor)
    private fun toDomain(row: ProjectEntity) = Project(ProjectId(row.id), row.title, row.goalId?.let(::GoalId), row.steps.lines().filter { it.isNotBlank() }.mapNotNull { line ->
        val parts = line.split('\t', limit = 5)
        if (parts.size < 5) return@mapNotNull null
        ProjectStep(ProjectStepId(parts[0]), parts[4], Duration.ofMinutes(parts[3].toLongOrNull() ?: 30), parts[1].toIntOrNull() ?: 0, parts[2].toBoolean())
    }, row.someday, row.waitingFor)
}

class RoomInboxRepository @Inject constructor(private val dao: InboxItemDao) : InboxRepository {
    override fun observeAll(): Flow<List<InboxItem>> = dao.observeAll().map { it.map(::toDomain) }
    override suspend fun getById(id: InboxItemId): InboxItem? = dao.getById(id.value)?.let(::toDomain)
    override suspend fun save(item: InboxItem) = dao.upsert(toEntity(item))
    override suspend fun delete(id: InboxItemId) = dao.delete(id.value)
    private fun toEntity(item: InboxItem) = InboxItemEntity(item.id.value, item.text, item.status.name, item.goalId?.value, item.projectId?.value, item.createdAt)
    private fun toDomain(row: InboxItemEntity) = InboxItem(InboxItemId(row.id), row.text, runCatching { InboxStatus.valueOf(row.status) }.getOrDefault(InboxStatus.INBOX), row.goalId?.let(::GoalId), row.projectId?.let(::ProjectId), row.createdAt)
}

class RoomMilestoneRepository @Inject constructor(private val dao: MilestoneDao) : MilestoneRepository {
    override fun observeAll(): Flow<List<Milestone>> = dao.observeAll().map { it.map { row -> Milestone(row.id, row.title, java.time.LocalDate.ofEpochDay(row.targetEpochDay), row.goalId?.let(::GoalId)) } }
    override suspend fun save(milestone: Milestone) = dao.upsert(MilestoneEntity(milestone.id, milestone.title, milestone.targetDate.toEpochDay(), milestone.goalId?.value))
    override suspend fun delete(id: String) = dao.delete(id)
}
