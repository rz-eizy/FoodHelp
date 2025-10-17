package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodhelp.screens.HomePage
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
        Category(
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardPreview(){
    MyApplicationTheme {

    }
}