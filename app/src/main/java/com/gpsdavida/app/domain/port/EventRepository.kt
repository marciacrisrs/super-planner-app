package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeAll(): Flow<List<Event>>
    suspend fun getById(id: EventId): Event?
    suspend fun save(event: Event)
    suspend fun delete(id: EventId)
}
