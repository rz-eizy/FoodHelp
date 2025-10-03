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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodhelp.components.HomeButton
import com.example.foodhelp.components.RecipeContent
import com.example.foodhelp.components.RecipeHeader
import com.example.foodhelp.ui.theme.AppBackground
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun RecipePage(){
    val nombre = "Nombre de receta"
    val description = "Esto es una descripción"
    Scaffold(
        topBar = {
            RecipeHeader(
                recipeName = nombre,
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
                    onClick = {/* Navegar a el inicio*/},
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
                description
            ) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecipe(){
    MyApplicationTheme { 
        RecipePage()
    }
}