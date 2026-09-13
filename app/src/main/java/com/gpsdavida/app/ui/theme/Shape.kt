package com.superplanner.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Geometry tokens for the soft, editorial Super Planner surfaces. */
val SuperPlannerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Shared spacing scale. Prefer these tokens over arbitrary per-screen values. */
object SuperPlannerSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Page = 20.dp
}

/** Shared component sizing tokens. */
object SuperPlannerDimensions {
    val TouchTarget = 48.dp
    val ButtonHeight = 52.dp
    val CompactButtonHeight = 44.dp
    val CardMinHeight = 96.dp
    val IconSmall = 20.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val Avatar = 40.dp
}
