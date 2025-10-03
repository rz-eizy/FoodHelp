package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodhelp.ui.theme.ComponentAccent
import com.example.foodhelp.ui.theme.SurfaceBackground

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
        FilledCard(
            description,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .weight(1f)
                .padding(bottom = 16.dp)
        )
        NextButton(
            nextClick,
            modifier = Modifier
        )
    }
}

@Composable
fun FilledCard(
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