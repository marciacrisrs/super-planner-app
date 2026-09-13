package com.superplanner.app.domain.model

/** Explicitly protected essentials for periods in which normal capacity is unavailable. */
data class MinimumViableDay(
    val essentialActivityIds: Set<ActivityInstanceId>,
    val label: String = "Essencial",
) {
    fun protects(activity: ActivityInstance): Boolean = activity.id in essentialActivityIds
}

/** Empty time remains empty unless the user has actually planned something for it. */
data class ProtectedLifeTime(
    val activityId: ActivityInstanceId,
    val protectedFromAutofill: Boolean = true,
)

data class LifeAreaContext(
    val lifeAreaId: String,
    val label: String,
)
