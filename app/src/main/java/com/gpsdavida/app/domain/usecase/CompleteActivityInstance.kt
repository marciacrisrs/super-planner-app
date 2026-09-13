package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import java.time.Clock
import javax.inject.Inject

class CompleteActivityInstance @Inject constructor(
    private val record: RecordActivityExecution,
    private val clock: Clock,
) {
    /** Completes an activity using the real end instant and returns the completed instance. */
    suspend operator fun invoke(activity: ActivityInstance): ActivityInstance =
        record.complete(activity, clock.instant())
}
