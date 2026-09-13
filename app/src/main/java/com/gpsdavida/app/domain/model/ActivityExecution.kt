package com.superplanner.app.domain.model

import java.time.Instant

data class ActivityExecution(
    val activityInstanceId: ActivityInstanceId,
    val status: ActivityStatus,
    val planned: TimeRange,
    val actualStart: Instant? = null,
    val actual: TimeRange?,
)
