package com.gpsdavida.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SuperPlannerTimelineTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun timeline_renders_all_items_and_states() {
        composeRule.setContent {
            MaterialTheme {
                SuperPlannerTimeline(
                    items = listOf(
                        SuperPlannerTimelineItem(
                            time = "08:00",
                            title = "Café da manhã",
                            state = SuperPlannerTimelineState.COMPLETED,
                        ),
                        SuperPlannerTimelineItem(
                            time = "09:00",
                            title = "Trabalho",
                            supportingText = "Atividade atual",
                            state = SuperPlannerTimelineState.CURRENT,
                        ),
                        SuperPlannerTimelineItem(
                            time = "10:30",
                            title = "Estudo",
                            state = SuperPlannerTimelineState.UPCOMING,
                        ),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("08:00").assertExists()
        composeRule.onNodeWithText("Café da manhã").assertExists()
        composeRule.onNodeWithText("09:00").assertExists()
        composeRule.onNodeWithText("Trabalho").assertExists()
        composeRule.onNodeWithText("Atividade atual").assertExists()
        composeRule.onNodeWithText("10:30").assertExists()
        composeRule.onNodeWithText("Estudo").assertExists()
    }

    @Test
    fun timeline_supporting_text_is_optional() {
        composeRule.setContent {
            MaterialTheme {
                SuperPlannerTimeline(
                    items = listOf(
                        SuperPlannerTimelineItem(
                            time = "12:00",
                            title = "Almoço",
                        ),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("12:00").assertExists()
        composeRule.onNodeWithText("Almoço").assertExists()
    }
}
