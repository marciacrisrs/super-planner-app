package com.superplanner.app.domain.model

import java.time.LocalDate

enum class PlanType { GENERIC, HEALTH, NUTRITION, FITNESS, STUDY, FINANCE, SELF_CARE }
enum class PlanStatus { ACTIVE, PAUSED, ENDED, REPLACED }

data class Plan(
    val id: String,
    val name: String,
    val objective: String,
    val type: PlanType = PlanType.GENERIC,
    val origin: String? = null,
    val status: PlanStatus = PlanStatus.ACTIVE,
    val validFrom: LocalDate? = null,
    val validUntil: LocalDate? = null,
    val sourceDocument: String? = null,
    val version: Int = 1,
)

data class PlanItem(
    val id: String,
    val planId: String,
    val title: String,
    val durationMinutes: Int = 30,
    val daysOfWeek: Set<java.time.DayOfWeek> = emptySet(),
    val time: java.time.LocalTime? = null,
    val recurrence: RecurrenceRule? = null,
    val notes: String? = null,
)
