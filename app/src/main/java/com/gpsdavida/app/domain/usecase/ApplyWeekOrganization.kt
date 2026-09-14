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
                ?: error("Cannot apply unknown activity ${proposed.id}")
            if (original.instance.flexibility == Flexibility.FIXED) {
                val originalDate = original.date
                val originalStart = original.instance.planned.start.atZone(zoneId).toLocalTime()
                val originalEnd = original.instance.planned.end.atZone(zoneId).toLocalTime()
                require(proposed.date == originalDate.toString()) { "Fixed activity ${proposed.id} cannot be moved" }
                require(proposed.startTime == originalStart.toString()) { "Fixed activity ${proposed.id} cannot be moved" }
                require(proposed.endTime == originalEnd.toString()) { "Fixed activity ${proposed.id} cannot be moved" }
            }

            val date = LocalDate.parse(proposed.date)
            val start = date.atTime(LocalTime.parse(proposed.startTime)).atZone(zoneId).toInstant()
            val end = date.atTime(LocalTime.parse(proposed.endTime)).atZone(zoneId).toInstant()
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
