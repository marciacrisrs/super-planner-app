package com.superplanner.app.data

import com.superplanner.app.data.local.EventDao
import com.superplanner.app.data.mapper.toDomain
import com.superplanner.app.data.mapper.toEntity
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import com.superplanner.app.domain.port.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomEventRepository @Inject constructor(
    private val dao: EventDao,
) : EventRepository {
    override fun observeAll(): Flow<List<Event>> = dao.observeAll().map { rows ->
        rows.map { it.toDomain() }
    }

    override suspend fun getById(id: EventId): Event? = dao.getById(id.value)?.toDomain()

    override suspend fun save(event: Event) {
        dao.upsert(event.toEntity())
    }

    override suspend fun delete(id: EventId) {
        dao.delete(id.value)
    }
}
