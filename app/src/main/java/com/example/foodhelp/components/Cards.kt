package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodhelp.ui.theme.ColorButton
import com.example.foodhelp.ui.theme.ComponentAccent
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SelectedToggle
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun RecipeCard(
    description: String,
    modifier: Modifier = Modifier
){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SurfaceBackground,
            contentColor = ComponentAccent
        ),
        modifier = modifier
    ) {
        RecipeCardContent(description)
    }
}

@Composable
fun HomeCard(
    modifier: Modifier = Modifier
){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SelectedToggle,
            contentColor = ColorButton
        ),
        modifier = modifier
    ) {
        HomeCategory(
            modifier = Modifier
        )
    }
}

@Composable
fun RecipeListCard(
    modifier: Modifier = Modifier
){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SelectedToggle,
            contentColor = ColorButton
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }
    }
}

private const val INGR_POR_ROW = 3
@Composable
fun IngredientsCard(
    modifier: Modifier = Modifier,
    ingredientList: List<String>,
    onRemoveIngredient:(String) -> Unit
){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SelectedToggle,
            contentColor = ColorButton
        ),
        modifier = modifier
    ) {
        IngredientCardContent(
            ingredientList,
            onRemoveIngredient,
            INGR_POR_ROW
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardPreview(){
    MyApplicationTheme {

    }
}