package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.EventEntity
import com.superplanner.app.domain.model.Event
import com.superplanner.app.domain.model.EventId
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.RecurrenceUnit
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant
import java.time.LocalDate

fun EventEntity.toDomain(): Event = Event(
    id = EventId(id),
    title = title,
    range = TimeRange(Instant.ofEpochMilli(startEpochMilli), Instant.ofEpochMilli(endEpochMilli)),
    recurrenceDays = recurrenceDays.toDaySet(),
    recurrenceInterval = recurrenceInterval.coerceAtLeast(1),
    recurrenceUnit = recurrenceUnit?.let { runCatching { RecurrenceUnit.valueOf(it) }.getOrNull() },
    recurrenceEndDate = recurrenceEndEpochDay?.let(LocalDate::ofEpochDay),
    priority = Priority.valueOf(priority),
)

fun Event.toEntity(): EventEntity = EventEntity(
    id = id.value,
    title = title,
    startEpochMilli = range.start.toEpochMilli(),
    endEpochMilli = range.end.toEpochMilli(),
    recurrenceDays = recurrenceDays.joinToString(",") { it.name },
    recurrenceInterval = recurrenceInterval,
    recurrenceUnit = recurrenceUnit?.name,
    recurrenceEndEpochDay = recurrenceEndDate?.toEpochDay(),
    priority = priority.name,
)
