package com.example.foodhelp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import com.example.foodhelp.ui.theme.ComponentAccent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.foodhelp.navigation.AppScreens
import com.example.foodhelp.ui.theme.MyApplicationTheme
import com.example.foodhelp.ui.theme.NaranjaSuave
import com.example.foodhelp.ui.theme.Salmon

@Composable
fun HomeSegmentedButton(
    modifier: Modifier = Modifier,
    navController: NavController
){
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(Icons.Filled.Home to "Inicio", Icons.Filled.Search to "Busqueda")
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, (icon, description) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { selectedIndex = index
                          when(index){
                              0 -> {  navController.navigate(AppScreens.HomeScreen.route)  }
                              1 -> { navController.navigate(AppScreens.IngredientScreen.route) }
                          }},
                selected = index == selectedIndex,
                label = {Text(description)},
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = description
                    )
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Salmon,
                    activeContentColor = NaranjaSuave,
                    inactiveContainerColor = NaranjaSuave,
                    inactiveContentColor = Salmon
                )
            )
        }
    }
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
               .height(50.dp)
               .padding(end = 8.dp),
           colors = ButtonDefaults.buttonColors(
               containerColor = Salmon,
               contentColor = ComponentAccent
           ),
           shape = RoundedCornerShape(20.dp)
       ) { Text("Ingredientes") }

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Salmon,
                contentColor = ComponentAccent
            ),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Guardar")}
    }
}


@Preview(showBackground = true)
@Composable
fun ButtonPreview(){
    MyApplicationTheme {
        val navController = rememberNavController()
        //HomeSegmentedButton(navController= navController)
        SaveAndIngredients({},{})
    }
}