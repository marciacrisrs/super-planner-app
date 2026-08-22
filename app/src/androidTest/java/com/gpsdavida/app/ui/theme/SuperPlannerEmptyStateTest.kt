package com.gpsdavida.app.ui.theme

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SuperPlannerEmptyStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_rendersGuidanceAndIcon() {
        composeRule.setContent {
            SuperPlannerEmptyState(
                title = "Seu dia está livre",
                description = "Adicione uma atividade quando quiser planejar seu próximo passo.",
                icon = SuperPlannerIcon.CALENDAR,
                iconContentDescription = "Calendário",
            )
        }

        composeRule.onNodeWithText("Seu dia está livre").assertIsDisplayed()
        composeRule.onNodeWithText("Adicione uma atividade quando quiser planejar seu próximo passo.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Calendário").assertIsDisplayed()
    }

    @Test
    fun emptyState_actionIsOptional() {
        composeRule.setContent {
            SuperPlannerEmptyState(
                title = "Nenhuma meta",
                description = "Comece escolhendo algo que importa para você.",
                icon = SuperPlannerIcon.GOAL,
            )
        }

        composeRule.onNodeWithText("Nenhuma meta").assertIsDisplayed()
        composeRule.onNodeWithText("Comece escolhendo algo que importa para você.").assertIsDisplayed()
    }
}
