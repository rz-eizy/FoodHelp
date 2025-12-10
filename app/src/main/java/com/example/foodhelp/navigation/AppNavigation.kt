package com.example.foodhelp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.foodhelp.screens.HomeScreen
import com.example.foodhelp.screens.IngredientScreen
import com.example.foodhelp.screens.RecipeListScreen
import com.example.foodhelp.screens.RecipeScreen



@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
){
    NavHost(navController = navController, startDestination = AppScreens.HomeScreen.route){
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(
            route = AppScreens.RecipeScreen.route,
            arguments = listOf(navArgument("recetaId") { type = NavType.LongType})
        ) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getLong("recetaId") ?: -1L
            RecipeScreen(navController, recetaId)
        }
        composable(route = AppScreens.IngredientScreen.route) {
            IngredientScreen(navController)
        }
        composable(
            route = AppScreens.RecListScreen.route,
            arguments = listOf(navArgument("categoria") { type = NavType.StringType})
        ) { backStackEntry ->
            val categoria = backStackEntry.arguments?.getString("categoria") ?: ""
            RecipeListScreen(navController, categoria)
        }
    }
}