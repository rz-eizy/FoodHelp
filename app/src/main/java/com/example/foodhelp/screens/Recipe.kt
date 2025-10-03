package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun RecipePage(){
    Scaffold(
        topBar = {/*    Contenido Superior  */},
        bottomBar = {/* Contenido Inferior   */}

    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceBackground),
            ){Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){/*    Contenido Central   */}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecipe(){
    MyApplicationTheme { 
        RecipePage()
    }
}