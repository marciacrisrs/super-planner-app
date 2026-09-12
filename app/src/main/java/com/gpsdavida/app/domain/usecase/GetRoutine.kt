package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.model.RoutineId
import com.superplanner.app.domain.port.RoutineRepository
import javax.inject.Inject

class GetRoutine @Inject constructor(
    private val routines: RoutineRepository,
) {
    suspend operator fun invoke(id: RoutineId): Routine? = routines.getById(id)
}
