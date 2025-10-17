package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodhelp.ui.theme.MyApplicationTheme

@Composable
fun Category(
    modifier: Modifier = Modifier,
){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Postres", secondName = "Masas")
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Carnes", secondName = "Desayuno")
        RowCategory(modifier = Modifier.weight(1f),
            firstOnClick = {},secondOnClick = {}, firstName = "Frituras", secondName = "Guardadas")
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

@Preview(showBackground = true)
@Composable
fun RowPreview(){
    MyApplicationTheme {
        Category()
    }
}