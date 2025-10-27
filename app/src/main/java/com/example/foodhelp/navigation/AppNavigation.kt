package com.example.foodhelp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodhelp.screens.HomeScreen
import com.example.foodhelp.screens.IngredientScreen
import com.example.foodhelp.screens.RecipeListScreen
import com.example.foodhelp.screens.RecipeScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.HomeScreen.route){
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(route = AppScreens.RecipeScreen.route) {
            RecipeScreen(navController)
        }
        composable(route = AppScreens.IngredientScreen.route) {
            IngredientScreen(navController)
        }
        composable(route = AppScreens.RecListScreen.route) {
            RecipeListScreen(navController)
        }
    }
}