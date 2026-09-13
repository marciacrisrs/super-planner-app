package com.superplanner.app.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.superplanner.app.ui.navigation.SuperPlannerNavHost
import com.superplanner.app.ui.onboarding.OnboardingScreen
import com.superplanner.app.ui.theme.SuperPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        setContent {
            SuperPlannerTheme {
                val prefs = getSharedPreferences("super_planner", MODE_PRIVATE)
                if (!prefs.getBoolean("onboarding_done", false)) {
                    OnboardingScreen(
                        onFinished = {
                            prefs.edit().putBoolean("onboarding_done", true).apply()
                        },
                    )
                } else {
                    SuperPlannerNavHost()
                }
            }
        }
    }
}
