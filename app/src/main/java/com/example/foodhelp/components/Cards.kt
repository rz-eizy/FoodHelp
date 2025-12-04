package com.example.foodhelp.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    onCategoryClick: (String) -> Unit,
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
            onCategoryClick = onCategoryClick,
            modifier = Modifier
        )
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