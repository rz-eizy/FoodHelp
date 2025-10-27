package com.example.foodhelp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foodhelp.ui.theme.AppBackground
import com.example.foodhelp.ui.theme.SelectedToggle
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun RecipeHeader(
    appName: String = "FoodHelp",
    recipeName: String,
    onIngredientsClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(AppBackground)
    ){
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(245.dp)
                .clip(RoundedCornerShape(
                    topStart = 0.dp, topEnd = 0.dp,
                    bottomStart = 100.dp, bottomEnd = 100.dp
                )),
            color = SelectedToggle
        ) {  }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White)
            }

            SaveAndIngredients(
                onIngredientsClick = onIngredientsClick,
                onSaveClick = onSaveClick
            )
        }
    }
}