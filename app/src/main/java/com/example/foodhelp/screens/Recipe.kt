package com.example.foodhelp.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodhelp.components.HomeButton
import com.example.foodhelp.components.IngredientsCard
import com.example.foodhelp.components.RecipeContent
import com.example.foodhelp.components.RecipeHeader
import com.example.foodhelp.components.RecipeIngredientCard
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
    var showIngredientsDialog by remember { mutableStateOf(false) }
    val receta = uiState.selectedReceta
    val name = receta?.nombre
    val description = receta?.descripcion
    val lista = receta?.ingredientes
    val listaString: MutableList<String> = mutableListOf()

    lista?.forEach { ingrediente ->
        ingrediente.nombre.let { listaString.add(it) }
    }
    BackHandler(enabled = showIngredientsDialog) {
        showIngredientsDialog = false
    }
    Scaffold(
        topBar = {
            RecipeHeader(
                recipeName = name.toString(),
                onIngredientsClick = {
                    showIngredientsDialog = true
                },
                onSaveClick = {/*Guardar la receta dentro de la lista de recetas del usuario*/},
                modifier = Modifier
                    .background(SurfaceBackground    )
                    .windowInsetsPadding(WindowInsets.statusBars)
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
                        .height(100.dp)
                        .padding(16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RecipeContent(
                    nextClick = {},
                    modifier = Modifier,
                    description.toString()
                )
            }
        }
        if (showIngredientsDialog && lista != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showIngredientsDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clickable(enabled = true, onClick = {})
                ) {
                    RecipeIngredientCard(
                        ingredientList = listaString,
                        modifier = Modifier
                            .width(350.dp)
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}
