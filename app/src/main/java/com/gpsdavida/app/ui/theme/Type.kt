package com.superplanner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.superplanner.app.R

private val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold),
)

private val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val SuperPlannerScriptFont = FontFamily(
    Font(R.font.pacifico_regular, FontWeight.Normal),
)

val SuperPlannerBrandScript = TextStyle(
    fontFamily = SuperPlannerScriptFont,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 32.sp,
)

/** Editorial hierarchy for the Super Planner. */
val SuperPlannerTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.7).sp),
        headlineLarge = headlineLarge.copy(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.35).sp),
        headlineMedium = headlineMedium.copy(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
        titleLarge = titleLarge.copy(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
        titleMedium = titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        bodyLarge = bodyLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
        bodyMedium = bodyMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
        labelLarge = labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
        labelMedium = labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp),
    )
}
