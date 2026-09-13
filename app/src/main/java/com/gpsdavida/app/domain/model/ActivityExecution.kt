package com.superplanner.app.domain.model

data class ActivityExecution(
    val activityInstanceId: ActivityInstanceId,
    val status: ActivityStatus,
    val planned: TimeRange,
    val actualStart: java.time.Instant?,
    val actual: TimeRange?,
)
