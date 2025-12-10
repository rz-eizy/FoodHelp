package com.example.foodhelp.navigation

sealed class AppScreens(val route: String) {
    object HomeScreen: AppScreens("home_screen")
    object IngredientScreen: AppScreens("ingredient_screen")
    object RecListScreen: AppScreens("recipe_list_screen/{categoria}"){
        fun createRoute(categoria: String) = "recipe_list_screen/$categoria"
    }

    object RecipeScreen: AppScreens("recipe_screen/{recetaId}") {
        fun createRoute(recetaId: Long) = "recipe_screen/$recetaId"
    }
}