package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import java.time.Clock
import javax.inject.Inject

class CompleteActivityInstance @Inject constructor(
    private val record: RecordActivityExecution,
    private val clock: Clock,
) {
    suspend operator fun invoke(activity: ActivityInstance) {
        val now = clock.instant()
        record.complete(activity, activity.planned.start, now)
    }
}
