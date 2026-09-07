package com.gpsdavida.app.data

import com.gpsdavida.app.data.local.ContextNoteEntity
import com.gpsdavida.app.data.local.LeisureDao
import com.gpsdavida.app.data.local.LeisureItemEntity
import com.gpsdavida.app.data.local.ReadingGoalEntity
import com.gpsdavida.app.domain.model.ContextNote
import com.gpsdavida.app.domain.model.LeisureItem
import com.gpsdavida.app.domain.model.LeisureKind
import com.gpsdavida.app.domain.model.LeisureStatus
import com.gpsdavida.app.domain.model.ReadingGoal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLeisureRepository @Inject constructor(private val dao: LeisureDao) {
    fun observeItems(): Flow<List<LeisureItem>> = dao.observeItems().map { it.map(::itemToDomain) }
    suspend fun saveItem(item: LeisureItem) = dao.upsertItem(LeisureItemEntity(item.id, item.title, item.kind.name, item.status.name, item.notes))
    suspend fun deleteItem(id: String) = dao.deleteItem(id)
    fun observeReadingGoals(): Flow<List<ReadingGoal>> = dao.observeReadingGoals().map { it.map(::goalToDomain) }
    suspend fun saveReadingGoal(goal: ReadingGoal) = dao.upsertReadingGoal(ReadingGoalEntity(goal.id, goal.title, goal.minutesPerSession, goal.sessionsPerWeek, goal.active))
    fun observeNotes(targetType: String, targetId: String): Flow<List<ContextNote>> = dao.observeNotes(targetType, targetId).map { it.map(::noteToDomain) }
    suspend fun saveNote(note: ContextNote) = dao.upsertNote(ContextNoteEntity(note.id, note.targetType, note.targetId, note.body, note.createdAtEpochMilli))
    suspend fun deleteNote(id: String) = dao.deleteNote(id)

    private fun itemToDomain(row: LeisureItemEntity) = LeisureItem(row.id, row.title, runCatching { LeisureKind.valueOf(row.kind) }.getOrDefault(LeisureKind.SERIES), runCatching { LeisureStatus.valueOf(row.status) }.getOrDefault(LeisureStatus.WANT), row.notes)
    private fun goalToDomain(row: ReadingGoalEntity) = ReadingGoal(row.id, row.title, row.minutesPerSession, row.sessionsPerWeek, row.active)
    private fun noteToDomain(row: ContextNoteEntity) = ContextNote(row.id, row.targetType, row.targetId, row.body, row.createdAtEpochMilli)
}
