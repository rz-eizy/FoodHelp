package com.example.foodhelp.components

import android.widget.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text

@Composable
fun BotonSegmentado(){

}

@Composable
fun SaveAndIngredients(
    onIngredientsClick: ()-> Unit,
    onSaveClick: ()-> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
       Button(
           onClick = onIngredientsClick,
           modifier = Modifier
               .weight(1f)
               .padding(end = 8.dp),
           shape = RoundedCornerShape(20.dp)
       ) { Text("Ingredientes") }

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Guardar")}
    }
}