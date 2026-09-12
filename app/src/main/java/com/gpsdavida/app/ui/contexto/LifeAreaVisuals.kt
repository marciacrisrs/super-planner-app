package com.superplanner.app.ui.contexto

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class LifeAreaVisual(val icon: ImageVector, val accent: Color)

fun lifeAreaVisual(name: String): LifeAreaVisual = when (name.trim().lowercase()) {
    "trabalho" -> LifeAreaVisual(Icons.Filled.BusinessCenter, Color(0xFF5B6C8F))
    "saúde", "saude" -> LifeAreaVisual(Icons.Filled.Favorite, Color(0xFFB85C72))
    "estudos" -> LifeAreaVisual(Icons.Filled.Book, Color(0xFF6D72A8))
    "casa" -> LifeAreaVisual(Icons.Filled.Home, Color(0xFF8B6F47))
    "finanças", "financas" -> LifeAreaVisual(Icons.Filled.AccountBalance, Color(0xFF5D8A6A))
    "autocuidado" -> LifeAreaVisual(Icons.Filled.Spa, Color(0xFFB27895))
    "lazer" -> LifeAreaVisual(Icons.Filled.Weekend, Color(0xFF8C73A7))
    "social" -> LifeAreaVisual(Icons.Filled.SelfImprovement, Color(0xFF7C8A64))
    else -> LifeAreaVisual(Icons.Filled.SelfImprovement, Color(0xFF777777))
}
