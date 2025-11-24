package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RecipeContent(
    nextClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        RecipeCard(
            description,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .weight(1f)
                .padding(bottom = 16.dp)
        )
        NextButton(
            nextClick,
            modifier = Modifier,
            text = "¿Comenzar?"
        )
    }
}

@Composable
fun HomeCategory(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Postres", secondName = "Masas")
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Carnes", secondName = "Desayuno")
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Frituras", secondName = "Guardadas")
    }
}

@Composable
fun IngredientCardContent(
    ingredientList: List<String>,
    onRemoveIngredient:(String) -> Unit,
    ingredientsRow: Int
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ingredientList.chunked(ingredientsRow).forEach { rowIngredients ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowIngredients.forEach { ingredientName ->
                    IngredientButton(
                        text = ingredientName,
                        modifier = Modifier,
                        onRemoveClick = {onRemoveIngredient(ingredientName)}
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCardContent(
    description: String
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = description,
            textAlign = TextAlign.Center
        )
    }
}
