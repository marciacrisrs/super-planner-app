package com.superplanner.app.domain.model

import java.time.Duration
import java.time.Instant

data class ActivityInstance(
    val id: ActivityInstanceId,
    val source: ActivitySource,
    val flexibility: Flexibility,
    val planned: TimeRange,
    val priority: Priority = Priority.IMPORTANT,
    val energy: Energy? = null,
    val contexts: Set<ExecutionContext> = emptySet(),
    val location: Location? = null,
    val bufferAfter: Duration? = null,
    val actual: TimeRange? = null,
    val status: ActivityStatus = ActivityStatus.PENDING,
    val dueAt: Instant? = null,
    val delayConsequence: DelayConsequence = DelayConsequence.NONE,
) {
    val plannedDuration: Duration get() = planned.duration

    val actualDuration: Duration?
        get() = actual?.duration

    /** Signed difference: actual duration minus planned duration. */
    val durationVariance: Duration?
        get() = actualDuration?.minus(plannedDuration)

    fun completed(actualRange: TimeRange): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be completed" }
        return copy(actual = actualRange, status = ActivityStatus.DONE)
    }

    fun skipped(): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be skipped" }
        return copy(actual = null, status = ActivityStatus.SKIPPED)
    }

    fun deferred(): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be deferred" }
        return copy(actual = null, status = ActivityStatus.DEFERRED)
    }
}

enum class DelayConsequence(val weight: Int) {
    NONE(0),
    LOW(1),
    MODERATE(2),
    HIGH(3),
    CRITICAL(4),
}
