package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.ai.OrganizeWeekResponse
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.WeeklyPlanning
import com.superplanner.app.domain.port.WeeklyPlanOverride
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Applies only an explicitly approved week proposal to the persisted schedule overrides. */
class ApplyWeekOrganization @Inject constructor(
    private val overrides: WeeklyPlanOverrideRepository,
) {
    suspend operator fun invoke(
        week: WeeklyPlanning,
        response: OrganizeWeekResponse,
        zoneId: ZoneId,
    ) {
        val current = week.activities.associateBy { it.instance.id.value }
        val existing = overrides.observe().first().associateBy { it.activityId }.toMutableMap()

        response.proposedItems.forEach { proposed ->
            val original = current[proposed.id]
                ?: throw IllegalArgumentException("Cannot apply unknown activity ${proposed.id}")

            val date = runCatching { LocalDate.parse(proposed.date) }.getOrElse {
                throw IllegalArgumentException("Invalid date for ${proposed.id}", it)
            }
            require(date in week.startDate..week.endDate) {
                "Activity ${proposed.id} must remain inside the approved week"
            }

            val startTime = runCatching { LocalTime.parse(proposed.startTime) }.getOrElse {
                throw IllegalArgumentException("Invalid start time for ${proposed.id}", it)
            }
            val endTime = runCatching { LocalTime.parse(proposed.endTime) }.getOrElse {
                throw IllegalArgumentException("Invalid end time for ${proposed.id}", it)
            }

            if (original.instance.flexibility == Flexibility.FIXED) {
                val originalDate = original.date
                val originalStart = original.instance.planned.start.atZone(zoneId).toLocalTime()
                val originalEnd = original.instance.planned.end.atZone(zoneId).toLocalTime()
                require(date == originalDate) { "Fixed activity ${proposed.id} cannot be moved" }
                require(startTime == originalStart) { "Fixed activity ${proposed.id} cannot be moved" }
                require(endTime == originalEnd) { "Fixed activity ${proposed.id} cannot be moved" }
            }

            val start = date.atTime(startTime).atZone(zoneId).toInstant()
            val end = date.atTime(endTime).atZone(zoneId).toInstant()
            require(start < end) { "Invalid time range for ${proposed.id}" }
            existing[proposed.id] = WeeklyPlanOverride(
                activityId = proposed.id,
                date = proposed.date,
                start = start,
                end = end,
            )
        }

        overrides.save(existing.values.toList())
    }
}
