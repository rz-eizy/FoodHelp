package com.example.foodhelp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.runner.AndroidJUnit4
import com.example.foodhelp.navigation.AppNavigation
import com.example.foodhelp.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class NavigationTest{
    @get:Rule
    val composeRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
    private lateinit var navController: TestNavHostController

    @Test
    fun navigate_home_to_recipe() {
        val placeholder = "Buscar Recetas"
        val searchQuery = "huevo frito simple"

        composeRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            MyApplicationTheme {
                AppNavigation(navController = navController)
            }
        }
        composeRule.onNodeWithText(placeholder, ignoreCase = true).performClick()
        composeRule.onNodeWithText(placeholder, ignoreCase = true).performTextInput(searchQuery)
        composeRule.onNodeWithText(searchQuery, ignoreCase = true).performImeAction()
        composeRule.waitForIdle()
        assertEquals("recipe_screen", navController.currentDestination?.route)
    }
}