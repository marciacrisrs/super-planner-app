package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import java.time.Clock
import javax.inject.Inject

class CompleteActivityInstance @Inject constructor(
    private val record: RecordActivityExecution,
    private val clock: Clock,
) {
    /** Completes an activity using the real end instant; the persisted real start is preserved. */
    suspend operator fun invoke(activity: ActivityInstance) {
        record.complete(activity, clock.instant())
    }
}
