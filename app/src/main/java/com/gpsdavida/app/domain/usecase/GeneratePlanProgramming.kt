package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.data.RoomPlanRepository
import com.gpsdavida.app.domain.model.Event
import com.gpsdavida.app.domain.model.EventId
import com.gpsdavida.app.domain.model.Priority
import com.gpsdavida.app.domain.model.Task
import com.gpsdavida.app.domain.model.TaskId
import com.gpsdavida.app.domain.model.Plan
import com.gpsdavida.app.domain.port.EventRepository
import com.gpsdavida.app.domain.port.TaskRepository
import java.time.Duration
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GeneratePlanProgramming @Inject constructor(
    private val plans: RoomPlanRepository,
    private val events: EventRepository,
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(plan: Plan, zone: ZoneId = ZoneId.systemDefault()) {
        val items = plans.observeItems(plan.id).first()
        items.forEach { item ->
            val startDate = plan.validFrom ?: java.time.LocalDate.now()
            if (item.time != null) {
                val firstDate = item.daysOfWeek.minByOrNull { it.value }
                    ?.let { day -> startDate.plusDays(((day.value - startDate.dayOfWeek.value + 7) % 7).toLong()) }
                    ?: startDate
                val start = java.time.ZonedDateTime.of(firstDate, item.time, zone).toInstant()
                events.save(
                    Event(
                        id = EventId("plan-${plan.id}-${item.id}"),
                        title = item.title,
                        range = com.gpsdavida.app.domain.model.TimeRange(start, start.plus(Duration.ofMinutes(item.durationMinutes.toLong()))),
                        recurrenceDays = item.daysOfWeek,
                        priority = Priority.IMPORTANT,
                    ),
                )
            } else {
                tasks.save(
                    Task(
                        id = TaskId("plan-${plan.id}-${item.id}-${UUID.randomUUID()}"),
                        title = item.title,
                        plannedDuration = Duration.ofMinutes(item.durationMinutes.toLong()),
                        priority = Priority.IMPORTANT,
                        due = startDate.atStartOfDay(zone).toInstant(),
                    ),
                )
            }
        }
    }
}
