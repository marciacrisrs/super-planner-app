package com.superplanner.app.ui.agora

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.activity.ComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgoraRecoveryCardInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun currentActivityState_explainsReplanningWithoutPresentingItAsFailure() {
        var replans = 0

        composeRule.setContent {
            MaterialTheme {
                AgoraRecoveryCard(
                    hasCurrentActivity = true,
                    onReplan = { replans++ },
                )
            }
        }

        composeRule.onNodeWithText("O dia mudou?").assertIsDisplayed()
        composeRule.onNodeWithText("Tudo bem ajustar a rota. Veja uma nova sugestão com base no que está acontecendo agora.").assertIsDisplayed()
        composeRule.onNodeWithText("Ver uma nova sugestão").performClick()
        assertEquals(1, replans)
    }

    @Test
    fun emptyState_stillProvidesExplicitRecoveryPath() {
        var replans = 0

        composeRule.setContent {
            MaterialTheme {
                AgoraRecoveryCard(
                    hasCurrentActivity = false,
                    onReplan = { replans++ },
                )
            }
        }

        composeRule.onNodeWithText("O dia mudou?").assertIsDisplayed()
        composeRule.onNodeWithText("Se algo mudou, você pode pedir uma nova sugestão para o momento atual.").assertIsDisplayed()
        composeRule.onNodeWithText("Ver uma nova sugestão").performClick()
        assertEquals(1, replans)
    }
}
