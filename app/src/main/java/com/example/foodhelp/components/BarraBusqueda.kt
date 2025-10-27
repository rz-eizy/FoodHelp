package com.example.foodhelp.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }


    SearchBar(
        query = query,
        onQueryChange = {query = it},
        onSearch ={
            onSearch(query)
            active = false
        },
        active = active,
        onActiveChange = {active = it},
        placeholder = { Text("Buscar Recetas") },
        modifier = modifier.fillMaxWidth()
    ) {
        /*
        Aqui se agrega lo que ocurre cuando se usa search bar
        */
    }
}