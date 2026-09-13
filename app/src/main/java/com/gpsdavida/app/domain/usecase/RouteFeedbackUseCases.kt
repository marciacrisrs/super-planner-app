package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.RouteFeedback
import com.superplanner.app.domain.model.RouteFeedbackId
import com.superplanner.app.domain.model.RouteFeedbackReason
import com.superplanner.app.domain.port.RouteFeedbackRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class RecordRouteFeedback @Inject constructor(
    private val repository: RouteFeedbackRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(activityId: ActivityInstanceId, reason: RouteFeedbackReason) {
        repository.save(
            RouteFeedback(
                id = RouteFeedbackId(UUID.randomUUID().toString()),
                activityId = activityId,
                reason = reason,
                createdAt = clock.instant(),
            ),
        )
    }
}

class ObserveRouteFeedback @Inject constructor(
    private val repository: RouteFeedbackRepository,
) {
    operator fun invoke(): Flow<List<RouteFeedback>> = repository.observeAll()
}