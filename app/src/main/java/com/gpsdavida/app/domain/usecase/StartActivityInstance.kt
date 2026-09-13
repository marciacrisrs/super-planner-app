package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import java.time.Clock
import javax.inject.Inject

class StartActivityInstance @Inject constructor(
    private val record: RecordActivityExecution,
    private val clock: Clock,
) {
    /** Starts a pending activity at the current real-world instant and persists it. */
    suspend operator fun invoke(activity: ActivityInstance) {
        record.start(activity, clock.instant())
    }
}
