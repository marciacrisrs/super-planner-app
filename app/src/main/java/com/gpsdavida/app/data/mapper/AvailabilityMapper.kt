package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.AvailabilityEntity
import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import com.superplanner.app.domain.model.AvailabilityKind
import com.superplanner.app.domain.model.LocalTimeWindow
import java.time.DayOfWeek
import java.time.LocalTime

fun AvailabilityEntity.toDomain(): Availability = Availability(
    id = AvailabilityId(id),
    dayOfWeek = DayOfWeek.of(dayOfWeek),
    window = LocalTimeWindow(
        start = LocalTime.of(startMinute / 60, startMinute % 60),
        end = LocalTime.of(endMinute / 60, endMinute % 60),
    ),
    kind = AvailabilityKind.valueOf(kind),
)

fun Availability.toEntity(): AvailabilityEntity = AvailabilityEntity(
    id = id.value,
    dayOfWeek = dayOfWeek.value,
    startMinute = window.start.hour * 60 + window.start.minute,
    endMinute = window.end.hour * 60 + window.end.minute,
    kind = kind.name,
)