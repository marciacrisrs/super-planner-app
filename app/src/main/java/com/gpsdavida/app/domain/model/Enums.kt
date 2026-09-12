package com.superplanner.app.domain.model

enum class Priority(val weight: Int) {
    REQUIRED(0),
    IMPORTANT(1),
    DESIRABLE(2),
    LEISURE(3),
}

enum class Energy {
    LOW,
    MEDIUM,
    HIGH,
}

enum class Flexibility {
    FIXED,
    FLEXIBLE,
}

enum class ActivityStatus {
    PENDING,
    DONE,
    SKIPPED,
    DEFERRED,
}

enum class AvailabilityKind {
    FREE,
    BLOCKED,
}
