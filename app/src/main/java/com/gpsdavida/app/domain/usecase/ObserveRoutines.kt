package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.Routine
import com.superplanner.app.domain.port.RoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRoutines @Inject constructor(
    private val routines: RoutineRepository,
) {
    operator fun invoke(): Flow<List<Routine>> = routines.observeAll()
}
