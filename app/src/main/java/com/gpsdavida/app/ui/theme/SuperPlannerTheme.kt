package com.superplanner.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SuperPlannerLightColors = lightColorScheme(
    primary = SuperPlannerColors.Terracotta,
    onPrimary = SuperPlannerColors.Surface,
    primaryContainer = SuperPlannerColors.TerracottaSoft,
    onPrimaryContainer = SuperPlannerColors.TerracottaDark,
    secondary = SuperPlannerColors.Rose,
    onSecondary = SuperPlannerColors.Ink,
    secondaryContainer = SuperPlannerColors.RoseSoft,
    onSecondaryContainer = SuperPlannerColors.Ink,
    tertiary = SuperPlannerColors.Sage,
    onTertiary = SuperPlannerColors.Ink,
    tertiaryContainer = SuperPlannerColors.SageSoft,
    onTertiaryContainer = SuperPlannerColors.Ink,
    background = SuperPlannerColors.Canvas,
    onBackground = SuperPlannerColors.Ink,
    surface = SuperPlannerColors.Surface,
    onSurface = SuperPlannerColors.Ink,
    surfaceVariant = SuperPlannerColors.SurfaceWarm,
    onSurfaceVariant = SuperPlannerColors.InkSoft,
    outline = SuperPlannerColors.Outline,
    error = SuperPlannerColors.Error,
)

@Composable
fun SuperPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SuperPlannerLightColors,
        typography = SuperPlannerTypography,
        shapes = SuperPlannerShapes,
        content = content,
    )
}
