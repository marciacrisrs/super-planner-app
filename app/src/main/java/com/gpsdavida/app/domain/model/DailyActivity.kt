package com.superplanner.app.domain.model

/** Materialized occurrence of an activity for a single day, ready for scheduling and execution. */
data class DailyActivity(
    val title: String,
    val instance: ActivityInstance,
)
