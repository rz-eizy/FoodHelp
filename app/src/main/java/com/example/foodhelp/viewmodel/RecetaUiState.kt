package com.example.foodhelp.viewmodel

import com.example.foodhelp.data.Receta

data class RecetaUiState (
    val query: String = "",
    val isLoading: Boolean = false,
    val recetasEncontradas: List<Receta> = emptyList(),
    val errorMessage: String? = null,
    val navigateToRecipeId: Long? = null
)