package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.RoutineId
import com.superplanner.app.domain.port.RoutineRepository
import javax.inject.Inject

class DeleteRoutine @Inject constructor(
    private val routines: RoutineRepository,
) {
    suspend operator fun invoke(id: RoutineId) = routines.delete(id)
}
