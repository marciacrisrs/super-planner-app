package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.Availability
import com.superplanner.app.domain.model.AvailabilityId
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

interface AvailabilityRepository {
    fun observeAll(): Flow<List<Availability>>
    fun observeForDay(dayOfWeek: DayOfWeek): Flow<List<Availability>>
    suspend fun save(availability: Availability)
    suspend fun delete(id: AvailabilityId)
}