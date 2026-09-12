package com.superplanner.app.domain.model

/**
 * Context in which an activity can be executed.
 *
 * Context is a soft execution constraint: when the user declares a current
 * context, activities requiring another context are not recommended.
 */
enum class ExecutionContext {
    HOME,
    OUTSIDE,
    COMPUTER,
    PHONE,
}
