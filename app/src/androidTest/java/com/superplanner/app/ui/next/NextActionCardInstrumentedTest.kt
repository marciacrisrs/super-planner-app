package com.superplanner.app.ui.next

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.activity.ComponentActivity
import java.time.LocalTime
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NextActionCardInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readyState_exposesSuggestionAndExecutesStartAction() {
        var starts = 0
        var snoozes = 0
        var completes = 0
        var swaps = 0

        composeRule.setContent {
            MaterialTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "Estudar francês",
                        durationMinutes = 60,
                        scheduledTime = LocalTime.of(19, 0),
                        explanation = "Cabe no tempo disponível e mantém sua prioridade de estudo.",
                        state = NextActionState.Ready,
                    ),
                    onStart = { starts++ },
                    onSnooze = { snoozes++ },
                    onComplete = { completes++ },
                    onSwap = { swaps++ },
                )
            }
        }

        composeRule.onNodeWithText("Uma sugestão para agora").assertIsDisplayed()
        composeRule.onNodeWithText("O que faço agora?").assertIsDisplayed()
        composeRule.onNodeWithText("Estudar francês").assertIsDisplayed()
        composeRule.onNodeWithText("Cabe no tempo disponível e mantém sua prioridade de estudo.").assertIsDisplayed()

        composeRule.onNodeWithText("Fazer agora").performClick()
        assertEquals(1, starts)
        assertEquals(0, completes)

        composeRule.onNodeWithText("Deixar para depois").performClick()
        composeRule.onNodeWithText("Ver outra opção").performClick()
        assertEquals(1, snoozes)
        assertEquals(1, swaps)
        assertEquals(0, completes)
    }

    @Test
    fun inProgressState_offersCompleteAndDoesNotInvokeReplanActions() {
        var completes = 0
        var snoozes = 0
        var swaps = 0

        composeRule.setContent {
            MaterialTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "Estudar francês",
                        state = NextActionState.InProgress,
                    ),
                    onComplete = { completes++ },
                    onSnooze = { snoozes++ },
                    onSwap = { swaps++ },
                )
            }
        }

        composeRule.onNodeWithText("Estudar francês").assertIsDisplayed()
        composeRule.onNodeWithText("Concluir").performClick()
        assertEquals(1, completes)

        composeRule.onNodeWithText("Deixar para depois").performClick()
        composeRule.onNodeWithText("Ver outra opção").performClick()
        assertEquals(0, snoozes)
        assertEquals(0, swaps)
    }

    @Test
    fun completedState_isNotActionableAndUsesFallbackForBlankTitle() {
        composeRule.setContent {
            MaterialTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "",
                        state = NextActionState.Completed,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Concluído").assertIsDisplayed()
        composeRule.onNodeWithText("Nada para mostrar agora.").assertIsDisplayed()
        composeRule.onNodeWithText("Fazer agora").assertDoesNotExist()
        composeRule.onNodeWithText("Ver outra opção").assertDoesNotExist()
        composeRule.onNodeWithText("Deixar para depois").assertDoesNotExist()
    }

    @Test
    fun emptyState_offersExplicitRecoveryAction() {
        var replans = 0

        composeRule.setContent {
            MaterialTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "",
                        state = NextActionState.Empty,
                    ),
                    onEmptyAction = { replans++ },
                )
            }
        }

        composeRule.onNodeWithText("Nada precisa de uma decisão agora.").assertIsDisplayed()
        composeRule.onNodeWithText("Ver uma nova sugestão").performClick()
        assertEquals(1, replans)
    }
}
