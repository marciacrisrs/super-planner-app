package com.superplanner.app.domain.ai

/** Minimal, structured context sent to the Gateway's organize-week feature. */
data class OrganizeWeekRequest(
    val weekStart: String,
    val timezone: String,
    val existingPlan: List<OrganizeWeekPlanItem> = emptyList(),
    val fixedCommitments: List<OrganizeWeekPlanItem> = emptyList(),
    val desires: List<OrganizeWeekPlanItem> = emptyList(),
    val logistics: List<OrganizeWeekLogisticConstraint> = emptyList(),
    val preferences: List<OrganizeWeekPreference> = emptyList(),
    val aiTips: List<String> = emptyList(),
    val capacity: OrganizeWeekCapacity? = null,
)

data class OrganizeWeekCapacity(
    val load: String,
    val totalCapacityMinutes: Int,
    val totalDesiredMinutes: Int,
    val totalRemainingMinutes: Int,
    val days: List<OrganizeWeekDayCapacity> = emptyList(),
    val reasons: List<String> = emptyList(),
)

data class OrganizeWeekDayCapacity(
    val date: String,
    val load: String,
    val schedulableMinutes: Int,
    val desiredMinutes: Int,
    val remainingMinutes: Int,
)

data class OrganizeWeekPlanItem(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationMinutes: Int? = null,
    val priority: String? = null,
    val kind: String? = null,
    val required: Boolean = false,
)

data class OrganizeWeekLogisticConstraint(
    val type: String,
    val minutes: Int,
    val beforeItemId: String? = null,
    val afterItemId: String? = null,
    val origin: String? = null,
    val destination: String? = null,
    val required: Boolean = true,
)

data class OrganizeWeekPreference(
    val key: String,
    val value: String,
)

data class OrganizeWeekResponse(
    val summary: OrganizeWeekSummary,
    val proposedItems: List<OrganizeWeekProposedItem>,
    val conflicts: List<OrganizeWeekConflict>,
    val opportunities: List<OrganizeWeekOpportunity>,
    val explanations: List<OrganizeWeekExplanation>,
    val model: String,
)

data class OrganizeWeekSummary(
    val fixedCommitmentsConsidered: Int,
    val desiresConsidered: Int,
    val commuteMinutesConsidered: Int,
    val preparationMinutesConsidered: Int,
    val aiSuggestionsConsidered: Int,
    val conflictsFound: Int,
    val opportunitiesFound: Int,
)

data class OrganizeWeekProposedItem(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val source: String,
    val fixed: Boolean,
    val reason: String?,
)

data class OrganizeWeekConflict(
    val id: String,
    val title: String,
    val affectedItemIds: List<String>,
    val reason: String,
    val severity: String,
)

data class OrganizeWeekOpportunity(
    val id: String,
    val title: String,
    val reason: String,
)

data class OrganizeWeekExplanation(
    val itemId: String?,
    val message: String,
)
