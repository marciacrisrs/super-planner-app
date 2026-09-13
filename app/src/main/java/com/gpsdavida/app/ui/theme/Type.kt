package com.superplanner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Editorial hierarchy for the Super Planner.
 *
 * The reference uses Playfair Display for titles, Inter for interface text,
 * and Pacifico only as a branding/signature treatment. Until the approved
 * font files are bundled, Compose's serif/sans-serif families act as the
 * platform-safe fallback while preserving the intended hierarchy.
 */
val SuperPlannerTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.7).sp,
        ),
        headlineLarge = headlineLarge.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.35).sp,
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.2).sp,
        ),
        titleLarge = titleLarge.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            lineHeight = 27.sp,
        ),
        titleMedium = titleMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 15.sp,
            lineHeight = 23.sp,
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        ),
        labelLarge = labelLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        ),
        labelMedium = labelMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 15.sp,
        ),
    )
}
