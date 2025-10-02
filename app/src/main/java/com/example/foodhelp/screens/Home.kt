package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodhelp.components.MySearchBar
import com.example.foodhelp.ui.theme.ComponentAccent
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun HomePage(){
    Scaffold(
        topBar = {
            MySearchBar(
                onSearch = {}, // Aqui implementar la navegacion entre pantallas
                modifier = Modifier.fillMaxWidth().background(SurfaceBackground)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                //Agregar aqui el boton segmentado
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceBackground),
        ){
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                //Agregar aqui las categorias
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun Previa(){
    MyApplicationTheme {
        HomePage()
    }
}