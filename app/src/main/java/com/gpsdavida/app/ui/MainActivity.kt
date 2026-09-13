package com.superplanner.app.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                val prefs = remember {
                    getSharedPreferences("super_planner", MODE_PRIVATE)
                }
                var onboardingDone by remember {
                    mutableStateOf(prefs.getBoolean("onboarding_done", false))
                }

                if (!onboardingDone) {
                    OnboardingScreen(
                        onFinished = {
                            prefs.edit().putBoolean("onboarding_done", true).apply()
                            onboardingDone = true
                        },
                    )
                } else {
                    SuperPlannerNavHost()
                }
            }
        }
    }
}
