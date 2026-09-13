package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.port.EventRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveEventsForDay @Inject constructor(
    private val events: EventRepository,
    private val clock: Clock,
) {
    operator fun invoke(date: LocalDate = LocalDate.now(clock)): Flow<List<Event>> {
        val zone = clock.zone
        return events.observeAll().map { list ->
            list.filter { it.occursOn(date, zone) }.sortedBy { it.range.start }
        }
    }
}
