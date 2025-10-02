package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodhelp.components.MySearchBar
import com.example.foodhelp.ui.theme.MyApplicationTheme

@Composable
fun HomePage(){

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD2D2D2)),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        MySearchBar(
            onSearch = {}, // Aqui implementar la navegacion entre pantallas
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Lo que va al medio
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            //Agregar aqui el boton segmentado
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