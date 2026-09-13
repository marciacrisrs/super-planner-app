package com.superplanner.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for the Super Planner visual palette.
 * Keep semantic usage in the theme/components instead of hard-coding colors in screens.
 */
object SuperPlannerColors {
    // Canvas and surfaces
    val Canvas = Color(0xFFFCF6F2)
    val Surface = Color(0xFFFFFBF8)
    val SurfaceWarm = Color(0xFFFFF3EE)

    // Typography and structure
    val Ink = Color(0xFF2E2E2E)
    val InkSoft = Color(0xFF625A56)
    val Outline = Color(0xFFE5D8D1)

    // Primary action / brand
    val Terracotta = Color(0xFFC56245)
    val TerracottaDark = Color(0xFFA64D36)
    val TerracottaSoft = Color(0xFFF5CFC5)

    // Editorial accent
    val Rose = Color(0xFFE69A8F)
    val RoseSoft = Color(0xFFFAD8D0)

    // Secondary contextual accents
    val Sage = Color(0xFFA7B89F)
    val SageSoft = Color(0xFFDCE7D9)
    val BlueGray = Color(0xFF8DA1B8)
    val BlueGraySoft = Color(0xFFDCE5EE)
    val Lavender = Color(0xFFDCCEF0)

    // Semantic states
    val Success = Color(0xFF5F896B)
    val Warning = Color(0xFFB17B4A)
    val Error = Color(0xFFAA5550)
}
