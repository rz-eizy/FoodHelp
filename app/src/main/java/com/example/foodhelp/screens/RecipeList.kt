package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodhelp.components.HomeButton
import com.example.foodhelp.components.RecipeListCard
import com.example.foodhelp.navigation.AppScreens
import com.example.foodhelp.ui.theme.AppBackground
import com.example.foodhelp.viewmodel.RecipeViewModel
import androidx.compose.runtime.*
import com.example.foodhelp.components.AlertDialogExample

@Composable
fun RecipeListScreen(
    navController: NavController,
    categoria: String,
    viewModel: RecipeViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.findRecipeByCategory(categoria)
    }
    val uiState by viewModel.uiState.collectAsState()
    val onRecipeClick: (Long) -> Unit = { recipeId ->
        navController.navigate(AppScreens.RecipeScreen.createRoute(recipeId))
    }
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FoodHelp",
                    fontSize = 50.sp
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ){
                HomeButton(
                    onClick = {
                        navController.navigate(AppScreens.HomeScreen.route)
                    },
                    modifier = Modifier
                        .width(125.dp)
                        .height(80.dp)
                        .padding(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                uiState.errorMessage != null ->{
                    AlertDialogExample(
                        onConfirmation = {
                            viewModel.onErrorHandled()
                            navController.popBackStack()
                        },
                        dialogTitle = "ERROR",
                        dialogText = uiState.errorMessage!!
                    )
                }
                else -> {
                    RecipeListCard(
                        recipes = uiState.recetas,
                        onRecipeClick = onRecipeClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}