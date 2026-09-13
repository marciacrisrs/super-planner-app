package com.superplanner.app.domain.model

data class ScheduleConflict(
    val activity: ActivityInstance,
    val reason: ScheduleConflictReason,
)

enum class ScheduleConflictReason {
    FIXED_OVERLAP,
    NO_AVAILABLE_WINDOW,
    DEPENDENCY_NOT_SATISFIED,
}

data class DailySchedule(
    val activities: List<ActivityInstance>,
    val conflicts: List<ScheduleConflict> = emptyList(),
) {
    val isConflictFree: Boolean get() = conflicts.isEmpty()
}
