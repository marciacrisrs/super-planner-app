package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.port.EventRepository
import javax.inject.Inject

class SaveEvent @Inject constructor(
    private val events: EventRepository,
) {
    suspend operator fun invoke(event: Event) {
        require(event.title.isNotBlank()) { "title" }
        events.save(event)
    }
}
