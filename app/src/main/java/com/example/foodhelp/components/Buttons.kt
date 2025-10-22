package com.example.foodhelp.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodhelp.R
import com.example.foodhelp.ui.theme.ColorButton
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

@Composable
fun NextButton(
    nextClick: () -> Unit,
    modifier: Modifier = Modifier,
    text : String
){
    Button(
        onClick = nextClick,
        modifier = modifier
            .height(65.dp)
            .width(250.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorButton,
            contentColor = ComponentAccent
        ),
        shape = RoundedCornerShape(40.dp)
    ) { Text(text) }
}

@Composable
fun IngredientButton(
    modifier: Modifier = Modifier,
    text: String,
    onRemoveClick: () -> Unit
){
    Button(
        onClick = onRemoveClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorButton,
            contentColor = ComponentAccent
        ),
        shape = RoundedCornerShape(40.dp)
    ) {
        Text(text)
    }
}