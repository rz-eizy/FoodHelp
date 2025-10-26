package com.example.foodhelp.navigation

sealed class AppScreens(val route: String) {
    object HomeScreen: AppScreens("home_screen")
    object RecipeScreen: AppScreens("recipe_screen")
    object IngredientScreen: AppScreens("ingredient_screen")
    object RecListScreen: AppScreens("reclist_screen")
}