package com.superplanner.app.domain.model

data class ActivityExecution(
    val activityInstanceId: ActivityInstanceId,
    val status: ActivityStatus,
    val planned: TimeRange,
    val actual: TimeRange?,
)
