package com.superplanner.app.domain.model

/** Optional context for responsibilities that involve someone beyond the user. */
enum class ResponsibilityScope {
    PERSONAL,
    CARE,
    SHARED,
}

data class ResponsibilityContext(
    val scope: ResponsibilityScope = ResponsibilityScope.PERSONAL,
    val participantLabel: String? = null,
)
