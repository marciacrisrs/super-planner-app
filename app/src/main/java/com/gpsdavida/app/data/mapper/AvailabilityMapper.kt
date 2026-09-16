package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.AvailabilityEntity
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.LocalTimeWindow
import java.time.DayOfWeek
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

fun AvailabilityEntity.toDomain(): Availability = Availability(
    id = AvailabilityId(id),
    dayOfWeek = DayOfWeek.of(dayOfWeek),
    window = LocalTimeWindow(
        start = LocalTime.of(startMinute / MINUTES_PER_HOUR, startMinute % MINUTES_PER_HOUR),
        end = LocalTime.of(endMinute / MINUTES_PER_HOUR, endMinute % MINUTES_PER_HOUR),
    ),
    kind = AvailabilityKind.valueOf(kind),
)

fun Availability.toEntity(): AvailabilityEntity = AvailabilityEntity(
    id = id.value,
    dayOfWeek = dayOfWeek.value,
    startMinute = window.start.hour * MINUTES_PER_HOUR + window.start.minute,
    endMinute = window.end.hour * MINUTES_PER_HOUR + window.end.minute,
    kind = kind.name,
)

