package com.superplanner.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for the Super Planner visual palette.
 * Keep semantic usage in the theme/components instead of hard-coding colors in screens.
 *
 * Surfaces intentionally carry transparency so cards and rounded rectangles feel layered
 * over the warm canvas instead of reading as opaque blocks.
 */
object SuperPlannerColors {
    // Canvas and translucent surfaces
    val Canvas = Color(0xFFFCF6F2)
    val Surface = Color(0xE6FFFBF8)
    val SurfaceWarm = Color(0xE6FFF3EE)

    // Typography and structure
    val Ink = Color(0xFF2E2E2E)
    val InkSoft = Color(0xFF625A56)
    val Outline = Color(0xB3E5D8D1)

    // Primary action / brand
    val Terracotta = Color(0xE6C56245)
    val TerracottaDark = Color(0xE6A64D36)
    val TerracottaSoft = Color(0xCCF5CFC5)

    // Editorial accent
    val Rose = Color(0xE6E69A8F)
    val RoseSoft = Color(0xCCFAD8D0)

    // Secondary contextual accents
    val Sage = Color(0xE6A7B89F)
    val SageSoft = Color(0xCCDCE7D9)
    val BlueGray = Color(0xE68DA1B8)
    val BlueGraySoft = Color(0xCCDCE5EE)
    val Lavender = Color(0xCCDCCEF0)

    // Semantic states
    val Success = Color(0xE65F896B)
    val Warning = Color(0xE6B17B4A)
    val Error = Color(0xE6AA5550)
}
