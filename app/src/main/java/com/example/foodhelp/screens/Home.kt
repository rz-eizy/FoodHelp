package com.example.foodhelp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foodhelp.components.BarraBusqueda
import com.example.foodhelp.components.BotonDividido

class Home {
    @Composable
    fun HomePage(){
        val barraBusqueda = BarraBusqueda()
        val botonSegmentado = BotonDividido()
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD2D2D2)),
            verticalArrangement = Arrangement.SpaceBetween
        ){
            barraBusqueda.MySearchBar()
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
                botonSegmentado.BotonSegmentado()
            }
        }
    }
}