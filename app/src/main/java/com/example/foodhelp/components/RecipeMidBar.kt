package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeContent(
    nextClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String
){
    Column(
        modifier = Modifier
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
