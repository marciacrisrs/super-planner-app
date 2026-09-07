package com.gpsdavida.app.domain.model

/** Structured reasons that can be surfaced by the Super Planner when choosing an action. */
enum class NextActionReason {
    CURRENTLY_ACTIVE,
    DUE_NOW,
    FIXED_COMMITMENT,
    AVAILABLE_IN_WINDOW,
    ENERGY_MATCH,
    CONTEXT_MATCH,
    DEPENDENCIES_SATISFIED,
    FLEXIBLE_SLOT,
    TRAVEL_FITS,
}
