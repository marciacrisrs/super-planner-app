package com.superplanner.app.ui.contexto

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.superplanner.app.R

data class LifeAreaVisual(
    @DrawableRes val iconRes: Int,
    val accent: Color,
)

/**
 * Visual vocabulary for life areas. These are the illustrated Super Planner assets,
 * not generic Material icons, so every area keeps the same editorial language.
 */
fun lifeAreaVisual(name: String): LifeAreaVisual = when (name.trim().lowercase()) {
    "trabalho" -> LifeAreaVisual(R.drawable.sp_category_work, Color(0xFF5B6C8F))
    "saúde", "saude" -> LifeAreaVisual(R.drawable.sp_category_health, Color(0xFFB85C72))
    "estudos" -> LifeAreaVisual(R.drawable.sp_category_study, Color(0xFF6D72A8))
    "casa" -> LifeAreaVisual(R.drawable.sp_category_home, Color(0xFF8B6F47))
    "finanças", "financas" -> LifeAreaVisual(R.drawable.sp_category_finance, Color(0xFF5D8A6A))
    "autocuidado" -> LifeAreaVisual(R.drawable.sp_category_selfcare, Color(0xFFB27895))
    "lazer" -> LifeAreaVisual(R.drawable.sp_category_leisure, Color(0xFF8C73A7))
    "viagens", "viagem" -> LifeAreaVisual(R.drawable.sp_category_travel, Color(0xFF6B88A8))
    "pets", "pet" -> LifeAreaVisual(R.drawable.sp_category_pets, Color(0xFF7C8A64))
    "projetos", "projeto" -> LifeAreaVisual(R.drawable.sp_category_projects, Color(0xFFB17B4A))
    "consultas", "consulta" -> LifeAreaVisual(R.drawable.sp_category_appointments, Color(0xFF8DA1B8))
    "hábitos", "habitos" -> LifeAreaVisual(R.drawable.sp_category_habits, Color(0xFFA7B89F))
    else -> LifeAreaVisual(R.drawable.sp_category_projects, Color(0xFF777777))
}
