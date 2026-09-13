package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.port.RoutineRepository
import javax.inject.Inject

class SaveRoutine @Inject constructor(
    private val routines: RoutineRepository,
) {
    suspend operator fun invoke(routine: Routine) = routines.save(routine)
}
