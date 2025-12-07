package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodhelp.components.HomeButton
import com.example.foodhelp.components.RecipeContent
import com.example.foodhelp.components.RecipeHeader
import com.example.foodhelp.navigation.AppScreens
import com.example.foodhelp.ui.theme.AppBackground
import com.example.foodhelp.ui.theme.SurfaceBackground
import com.example.foodhelp.viewmodel.RecipeViewModel

@Composable
fun RecipeScreen(
    navController: NavController,
    recetaId: Long,
    viewModel: RecipeViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.findRecipeById(recetaId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val receta = uiState.selectedReceta
    val name = receta?.nombre
    val description = receta?.descripcion
    Scaffold(
        topBar = {
            RecipeHeader(
                recipeName = name.toString(),
                onIngredientsClick = {/*Desplegar pantalla ingredientes*/},
                onSaveClick = {/*Guardar la receta dentro de la lista de recetas del usuario*/},
                modifier = Modifier.background(SurfaceBackground    )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(SurfaceBackground),
                contentAlignment = Alignment.Center
            ){
                HomeButton(
                    onClick = { navController.navigate(AppScreens.HomeScreen.route) },
                    modifier = Modifier
                        .width(125.dp)
                        .height(75.dp)
                        .padding(16.dp)
                )
            }
        }

    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground),
            ){Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){ RecipeContent(
                nextClick = {},
                modifier = Modifier,
                description.toString()
            ) }
        }
    }
}