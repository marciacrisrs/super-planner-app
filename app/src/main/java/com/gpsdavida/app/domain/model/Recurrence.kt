package com.gpsdavida.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/** Generic recurrence definition shared by scheduled activities. */
data class RecurrenceRule(
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val interval: Int = 1,
    val unit: RecurrenceUnit = RecurrenceUnit.WEEK,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
) {
    init {
        require(interval > 0) { "Recurrence interval must be positive" }
        require(endDate == null || !endDate.isBefore(startDate)) { "Recurrence end cannot precede start" }
    }
}

enum class RecurrenceUnit {
    DAY,
    WEEK,
    MONTH,
    YEAR,
}
