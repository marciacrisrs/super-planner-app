package com.superplanner.app.domain.model

import java.time.Instant

@JvmInline
value class RouteFeedbackId(val value: String)

enum class RouteFeedbackReason {
    HELPFUL,
    WANTED_OTHER,
    DURATION_WRONG,
    TIME_WRONG,
    PLANNER_WRONG,
}

data class RouteFeedback(
    val id: RouteFeedbackId,
    val activityId: ActivityInstanceId,
    val reason: RouteFeedbackReason,
    val createdAt: Instant,
)