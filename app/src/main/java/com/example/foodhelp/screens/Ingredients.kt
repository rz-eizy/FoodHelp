package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodhelp.components.IngredientsCard
import com.example.foodhelp.components.MySearchBar
import com.example.foodhelp.components.MySegmentedButton
import com.example.foodhelp.components.NextButton
import com.example.foodhelp.ui.theme.AppBackground
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun SearchIngredients(){
    val testIngredients = listOf(
        "Queso",
        "Huevo",
        "Tomate",
        "Pimienta",
        "Champiñones",
        "Cebolla",
        "Ajo",
        "Espárragos"
    )
    Scaffold(
        topBar = {
            MySearchBar(
                onSearch = {/* AGREGAR LOS INPUT A UNA LISTA */},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBackground)
            )
        }
        ,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                MySegmentedButton(
                    modifier = Modifier
                        .background(SurfaceBackground)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                IngredientsCard(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .height(550.dp),
                    ingredientList = testIngredients,
                    onRemoveIngredient = {/* ELIMINAR INGREDIENTE DE LA LISTA */}
                )
                NextButton(
                    nextClick = {/* NAVEGAR A SIGUIENTE PANTALLA */},
                    modifier = Modifier.padding(16.dp),
                    text = "Confirmar"
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIngredients(){
    MyApplicationTheme {
        SearchIngredients()
    }
}