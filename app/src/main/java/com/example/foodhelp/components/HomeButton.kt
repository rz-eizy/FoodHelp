package com.example.foodhelp.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import com.example.foodhelp.R
import com.example.foodhelp.ui.theme.ComponentAccent
import com.example.foodhelp.ui.theme.SurfaceBackground

@Composable
fun HomeButton(
    onClick:()-> Unit,
    modifier: Modifier= Modifier
){
    IconButton(
        onClick = onClick,
        modifier= modifier,
        colors = IconButtonColors(
            containerColor = ComponentAccent,
            contentColor = SurfaceBackground,
            disabledContentColor = ComponentAccent,
            disabledContainerColor = ComponentAccent
        )
    ) {
        Icon(
            painterResource(R.drawable.home_logo),
            contentDescription = "Home icon"
        )
    }
}