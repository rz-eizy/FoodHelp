package com.example.foodhelp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodhelp.data.Ingrediente
import com.example.foodhelp.data.Receta
import com.example.foodhelp.ui.theme.ColorButton
import com.example.foodhelp.ui.theme.SelectedToggle

@Composable
fun RecipeListCard(
    modifier: Modifier = Modifier,
    recipes: List<Receta>,
    onRecipeClick: (Long) -> Unit
){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SelectedToggle,
            contentColor = ColorButton
        ),
        modifier = modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecipeList(recipes, onRecipeClick)
        }
    }
}

@Composable
fun RecipeList(
    recipes: List<Receta>,
    onRecipeClick: (Long) -> Unit
){
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { recipe ->
            RecipeView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clickable{ onRecipeClick(recipe.id) },
                recipeName = recipe.nombre,
                ingredientList = recipe.ingredientes
            )
        }
    }
}

@Composable
fun RecipeView(
    modifier: Modifier = Modifier,
    recipeName: String,
    ingredientList: List<Ingrediente>
) {
    Column(
        modifier = modifier
            .background(Color(0xFF6A6A6A), shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .heightIn(min = 100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recipeName,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            ingredientList.take(3).forEachIndexed { index, igd ->
                IngredientChip(name = igd, 0)
                if (index < 2) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            if (ingredientList.size > 3) {
                Spacer(modifier = Modifier.width(8.dp))
                IngredientChip(name = ingredientList[3], 1)
            }
        }
    }
}

@Composable
fun IngredientChip(name: Ingrediente, x: Int) {
    Surface(
        color = Color(0xFFC7C7C7),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text =
                if (x != 0){
                    name.nombre
                } else{"..."},
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}