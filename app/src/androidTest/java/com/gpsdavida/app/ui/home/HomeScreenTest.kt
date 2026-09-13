package com.superplanner.app.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.superplanner.app.ui.next.NextActionCard
import com.superplanner.app.ui.next.NextActionState
import com.superplanner.app.ui.next.NextActionUiModel
import com.superplanner.app.ui.theme.SuperPlannerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nextAction_canBeStartedWithoutBecomingASecondPlanner() {
        var started = false

        composeRule.setContent {
            SuperPlannerTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "Estudar francês",
                        durationMinutes = 30,
                    ),
                    onStart = { started = true },
                )
            }
        }

        composeRule.onNodeWithText("O que faço agora?").assertIsDisplayed()
        composeRule.onNodeWithText("Estudar francês").assertIsDisplayed()
        composeRule.onNodeWithText("Começar").performClick()

        assertTrue(started)
    }

    @Test
    fun nextAction_emptyState_remains_focused() {
        composeRule.setContent {
            SuperPlannerTheme {
                NextActionCard(
                    model = NextActionUiModel(
                        title = "",
                        state = NextActionState.Empty,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Você está em dia.").assertIsDisplayed()
        composeRule.onNodeWithText("Quando houver uma próxima ação, ela aparecerá aqui.").assertIsDisplayed()
    }
}
