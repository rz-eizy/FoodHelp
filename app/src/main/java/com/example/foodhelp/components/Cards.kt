package com.example.foodhelp.components

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodhelp.ui.theme.ColorButton
import com.example.foodhelp.ui.theme.ComponentAccent
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SelectedToggle
import com.example.foodhelp.ui.theme.SurfaceBackground
import com.example.foodhelp.ui.theme.Cuarzo
import com.example.foodhelp.ui.theme.RojoFuerte

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
            containerColor = RojoFuerte,
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

@Composable
fun RecipeIngredientCard(
    modifier: Modifier = Modifier,
    ingredientList: List<String>
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Cuarzo
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            ingredientList.forEach { ingredientName ->
                IngredientButton(
                    text = ingredientName,
                    onRemoveClick = { }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CardPreview(){
    MyApplicationTheme {

    }
}