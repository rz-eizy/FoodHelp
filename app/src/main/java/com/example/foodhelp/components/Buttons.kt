package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ButtonRecipe(
    onClick: () -> Unit,
    recipeName: String,
    ingredientList: List<String>,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = ComponentAccent,
            contentColor = ColorButton
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(recipeName)
        }
    }
}

@Composable
fun RowCategory(
    modifier: Modifier = Modifier,
    firstOnClick : () -> Unit,
    secondOnClick : () -> Unit,
    firstName: String,
    secondName: String
){
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize().padding(8.dp)){
            Button(
                onClick = firstOnClick,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text(text = firstName, fontSize = 24.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.SansSerif) }
        }
        Box(modifier = Modifier.weight(1f).fillMaxSize().padding(8.dp)){
            Button(
                onClick = secondOnClick,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text(text = secondName, fontSize = 24.sp, fontStyle = FontStyle.Italic, fontFamily = FontFamily.SansSerif) }
        }
    }
}