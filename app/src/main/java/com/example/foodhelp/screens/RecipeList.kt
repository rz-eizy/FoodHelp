package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodhelp.components.HomeButton
import com.example.foodhelp.components.RecipeListCard
import com.example.foodhelp.ui.theme.AppBackground

@Composable
fun RecipeListScreen(navController: NavController){
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FoodHelp",
                    fontSize = 50.sp
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ){
                HomeButton(
                    onClick = {/* Navegar a el inicio*/},
                    modifier = Modifier
                        .width(125.dp)
                        .height(75.dp)
                        .padding(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBackground),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecipeListCard(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(650.dp)
            )
        }
    }
}