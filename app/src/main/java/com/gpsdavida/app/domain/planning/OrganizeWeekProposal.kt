package com.superplanner.app.domain.planning

import java.time.LocalDate
import java.time.LocalTime

data class WeekPlanItemSnapshot(
    val id: String,
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val fixed: Boolean,
)

data class WeekOrganizationProposal(
    val original: List<WeekPlanItemSnapshot>,
    val proposed: List<WeekPlanItemSnapshot>,
    val conflicts: List<String>,
    val opportunities: List<String>,
    val explanations: List<String>,
) {
    init {
        validateFixedItems()
        validateTimeOrder()
    }

    val changes: List<WeekPlanItemChange>
        get() = original.mapNotNull { before ->
            val after = proposed.firstOrNull { it.id == before.id } ?: return@mapNotNull WeekPlanItemChange.Removed(before)
            if (before == after) null else WeekPlanItemChange.Changed(before, after)
        } + proposed.filter { candidate -> original.none { it.id == candidate.id } }.map(WeekPlanItemChange::Added)

    fun apply(): AppliedWeekOrganization = AppliedWeekOrganization(
        original = original,
        applied = proposed,
        changes = changes,
    )

    private fun validateFixedItems() {
        val proposedById = proposed.associateBy { it.id }
        original.filter { it.fixed }.forEach { fixed ->
            val replacement = proposedById[fixed.id]
                ?: error("Fixed item ${fixed.id} cannot be removed by organization")
            require(replacement == fixed) { "Fixed item ${fixed.id} cannot be changed silently" }
        }
    }

    private fun validateTimeOrder() {
        proposed.groupBy { it.date }.values.forEach { items ->
            val timed = items.filter { it.startTime != null && it.endTime != null }.sortedBy { it.startTime }
            timed.zipWithNext().forEach { (current, next) ->
                require(current.endTime!! <= next.startTime!!) {
                    "Proposed week contains an overlap on ${current.date}"
                }
            }
        }
    }
}

sealed interface WeekPlanItemChange {
    data class Added(val item: WeekPlanItemSnapshot) : WeekPlanItemChange
    data class Removed(val item: WeekPlanItemSnapshot) : WeekPlanItemChange
    data class Changed(val before: WeekPlanItemSnapshot, val after: WeekPlanItemSnapshot) : WeekPlanItemChange
}

data class AppliedWeekOrganization(
    val original: List<WeekPlanItemSnapshot>,
    val applied: List<WeekPlanItemSnapshot>,
    val changes: List<WeekPlanItemChange>,
)
