package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodhelp.components.AlertDialogExample
import com.example.foodhelp.components.HomeCard
import com.example.foodhelp.components.MySearchBar
import com.example.foodhelp.components.HomeSegmentedButton
import com.example.foodhelp.navigation.AppScreens
import com.example.foodhelp.ui.theme.SurfaceBackground
import com.example.foodhelp.viewmodel.RecipeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.navigateToRecipeId) {
        val recipeId = uiState.navigateToRecipeId
        if (recipeId != null && recipeId > 0){
            navController.navigate(AppScreens.RecipeScreen.createRoute(recipeId))
            viewModel.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            MySearchBar(
                onSearch = {query ->
                    viewModel.buscarRecetas(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBackground)
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBackground),
                contentAlignment = Alignment.Center
            ) {
                HomeSegmentedButton(
                    modifier = Modifier
                        .background(SurfaceBackground)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){

            }
        } else if (uiState.errorMessage != null){
            AlertDialogExample(
                onConfirmation = {
                    viewModel.onErrorHandled()
                },
                dialogTitle = "ERROR",
                dialogText = uiState.errorMessage!!
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceBackground),
        ){
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                HomeCard(
                    onCategoryClick = { category ->
                        navController.navigate(AppScreens.RecListScreen.createRoute(category))
                    },
                    modifier = Modifier
                        .size(height = 550.dp, width = 325.dp)
                )
            }
        }
    }
}