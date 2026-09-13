package com.superplanner.app.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class SuperPlannerColorsTest {
    @Test
    fun officialPaletteKeepsWarmPlannerCanvas() {
        assertArgbEquals(0xFFFCF6F2.toInt(), SuperPlannerColors.Canvas.toArgb())
        assertArgbEquals(0xFFFFFBF8.toInt(), SuperPlannerColors.Surface.toArgb())
    }

    @Test
    fun officialPaletteKeepsTerracottaAsPrimaryAccent() {
        assertArgbEquals(0xFFC56245.toInt(), SuperPlannerColors.Terracotta.toArgb())
        assertArgbEquals(0xFFF5CFC5.toInt(), SuperPlannerColors.TerracottaSoft.toArgb())
    }

    @Test
    fun officialPaletteContainsSecondaryOrganicAccents() {
        assertArgbEquals(0xFFE69A8F.toInt(), SuperPlannerColors.Rose.toArgb())
        assertArgbEquals(0xFFA7B89F.toInt(), SuperPlannerColors.Sage.toArgb())
        assertArgbEquals(0xFF8DA1B8.toInt(), SuperPlannerColors.BlueGray.toArgb())
    }

    private fun assertArgbEquals(expected: Int, actual: Int) {
        assertEquals(String.format("expected=0x%08X actual=0x%08X", expected, actual), expected, actual)
    }
}
