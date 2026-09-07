package com.gpsdavida.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gpsdavida.app.ui.navigation.GpsNavHost
import com.gpsdavida.app.ui.onboarding.OnboardingScreen
import com.gpsdavida.app.ui.theme.GpsDaVidaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpsDaVidaTheme {
                val prefs = getSharedPreferences("super_planner", MODE_PRIVATE)
                if (!prefs.getBoolean("onboarding_done", false)) {
                    OnboardingScreen(
                        onFinished = {
                            prefs.edit().putBoolean("onboarding_done", true).apply()
                        },
                    )
                } else {
                    GpsNavHost()
                }
            }
        }
    }
}
