package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import com.superplanner.app.domain.port.EventRepository
import javax.inject.Inject

class GetEvent @Inject constructor(
    private val events: EventRepository,
) {
    suspend operator fun invoke(id: EventId): Event? = events.getById(id)
}
