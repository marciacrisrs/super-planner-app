package com.superplanner.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class SuperPlannerColorsTest {
    @Test
    fun officialPaletteKeepsWarmPlannerCanvas() {
        assertEquals(Color(0xFFFCF6F2), SuperPlannerColors.Canvas)
        assertEquals(Color(0xFFFFFBF8), SuperPlannerColors.Surface)
    }

    @Test
    fun officialPaletteKeepsTerracottaAsPrimaryAccent() {
        assertEquals(Color(0xFFC56245), SuperPlannerColors.Terracotta)
        assertEquals(Color(0xFFF5CFC5), SuperPlannerColors.TerracottaSoft)
    }

    @Test
    fun officialPaletteContainsSecondaryOrganicAccents() {
        assertEquals(Color(0xFFE69A8F), SuperPlannerColors.Rose)
        assertEquals(Color(0xFFA7B89F), SuperPlannerColors.Sage)
        assertEquals(Color(0xFF8DA1B8), SuperPlannerColors.BlueGray)
    }
}
