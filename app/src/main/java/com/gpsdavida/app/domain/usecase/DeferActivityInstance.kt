package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityInstance
import javax.inject.Inject

class DeferActivityInstance @Inject constructor(
    private val record: RecordActivityExecution,
) {
    suspend operator fun invoke(activity: ActivityInstance) {
        record.defer(activity)
    }
}
