package com.sorokinuladzimir.homebarassistant.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class EmptyStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsMessage() {
        composeRule.setContent {
            EmptyState(message = "nothing here")
        }

        composeRule.onNodeWithText("nothing here").assertIsDisplayed()
    }
}
