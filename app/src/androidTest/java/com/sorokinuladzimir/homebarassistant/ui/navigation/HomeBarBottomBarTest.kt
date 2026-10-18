package com.sorokinuladzimir.homebarassistant.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeBarBottomBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun showsAllTabsAndReportsSelection() {
        var selected = -1
        composeRule.setContent {
            HomeBarTheme {
                HomeBarBottomBar(selectedIndex = 0, onSelect = { selected = it })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.bottom_bar_search)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.bottom_bar_drinks)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.bottom_bar_ingredients)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.bottom_bar_ingredients)).performClick()

        assertEquals(2, selected)
    }
}
