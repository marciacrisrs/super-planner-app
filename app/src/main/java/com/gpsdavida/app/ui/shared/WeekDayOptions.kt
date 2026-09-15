package com.superplanner.app.ui.shared

import com.superplanner.app.R
import java.time.DayOfWeek

fun weekDayOptions(): List<Pair<DayOfWeek, Int>> = listOf(
    DayOfWeek.MONDAY to R.string.day_mon,
    DayOfWeek.TUESDAY to R.string.day_tue,
    DayOfWeek.WEDNESDAY to R.string.day_wed,
    DayOfWeek.THURSDAY to R.string.day_thu,
    DayOfWeek.FRIDAY to R.string.day_fri,
    DayOfWeek.SATURDAY to R.string.day_sat,
    DayOfWeek.SUNDAY to R.string.day_sun,
)
