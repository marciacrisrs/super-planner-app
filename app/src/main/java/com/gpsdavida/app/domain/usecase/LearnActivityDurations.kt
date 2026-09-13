package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstanceId
import java.time.Duration
import javax.inject.Inject

/** Learns typical durations only from repeated completed observations. */
class LearnActivityDurations @Inject constructor() {
    companion object {
        private const val MIN_OBSERVATIONS = 3
    }

    operator fun invoke(executions: List<ActivityExecution>): Map<ActivityInstanceId, Duration> =
        executions
            .filter { it.actual != null }
            .groupBy { it.activityInstanceId }
            .mapNotNull { (id, history) ->
                if (history.size < MIN_OBSERVATIONS) return@mapNotNull null
                val durations = history.mapNotNull { it.actual?.duration }
                if (durations.size < MIN_OBSERVATIONS) return@mapNotNull null
                val sorted = durations.sortedBy { it.toMillis() }
                val median = sorted[sorted.lastIndex / 2]
                id to median
            }
            .toMap()
}
