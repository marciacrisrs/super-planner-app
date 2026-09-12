package com.superplanner.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class SuperPlannerColorsTest {
    @Test
    fun officialPaletteKeepsWarmPlannerCanvas() {
        assertEquals(Color(0xFFFBF7F4), SuperPlannerColors.Canvas)
        assertEquals(Color(0xFFFFFDFC), SuperPlannerColors.Surface)
    }

    @Test
    fun officialPaletteKeepsTerracottaAsPrimaryAccent() {
        assertEquals(Color(0xFFB9655F), SuperPlannerColors.Terracotta)
        assertEquals(Color(0xFFE9C5C0), SuperPlannerColors.TerracottaSoft)
    }

    @Test
    fun officialPaletteContainsSecondaryOrganicAccents() {
        assertEquals(Color(0xFFD88E95), SuperPlannerColors.Rose)
        assertEquals(Color(0xFFA7B3A4), SuperPlannerColors.Sage)
        assertEquals(Color(0xFF8C9AA7), SuperPlannerColors.BlueGray)
    }
}
