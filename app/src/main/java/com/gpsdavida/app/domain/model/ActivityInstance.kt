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
    val actualStart: Instant? = null,
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

    /** Marks the activity as actively executing at the supplied real-world instant. */
    fun started(at: Instant): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be started" }
        return copy(actualStart = at, actual = null, status = ActivityStatus.IN_PROGRESS)
    }

    /** Completes an active activity using the persisted real start and supplied real end. */
    fun completed(actualRange: TimeRange): ActivityInstance {
        require(status == ActivityStatus.IN_PROGRESS) { "Only in-progress activities can be completed" }
        require(actualStart != null) { "In-progress activities must have an actual start" }
        require(actualRange.start == actualStart) { "Completion must preserve the actual start" }
        return copy(actual = actualRange, status = ActivityStatus.DONE)
    }

    fun skipped(): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be skipped" }
        return copy(actual = null, actualStart = null, status = ActivityStatus.SKIPPED)
    }

    fun deferred(): ActivityInstance {
        require(status == ActivityStatus.PENDING) { "Only pending activities can be deferred" }
        return copy(actual = null, actualStart = null, status = ActivityStatus.DEFERRED)
    }
}

enum class DelayConsequence(val weight: Int) {
    NONE(0),
    LOW(1),
    MODERATE(2),
    HIGH(3),
    CRITICAL(4),
}
