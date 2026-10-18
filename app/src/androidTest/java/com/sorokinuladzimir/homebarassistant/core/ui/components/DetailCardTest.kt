package com.sorokinuladzimir.homebarassistant.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class DetailCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTitleAndContent() {
        composeRule.setContent {
            DetailCard(title = "Description") {
                Text("Rum and lime")
            }
        }

        composeRule.onNodeWithText("Description").assertIsDisplayed()
        composeRule.onNodeWithText("Rum and lime").assertIsDisplayed()
    }
}
