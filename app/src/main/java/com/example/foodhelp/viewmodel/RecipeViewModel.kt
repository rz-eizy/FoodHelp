package com.example.foodhelp.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhelp.repository.RecetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecetaRepository
) : ViewModel() {
    @VisibleForTesting
    internal var dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _uiState = MutableStateFlow(RecetaUiState())
    val uiState: StateFlow<RecetaUiState> = _uiState.asStateFlow()

    fun buscarRecetas(query: String) {
        val trimmedQury = query.trim()
        if (trimmedQury.isBlank()){
            _uiState.update {
                it.copy(
                    errorMessage = "La busqueda no puede estar vacia",
                    recetasEncontradas = emptyList(),
                    query = trimmedQury,
                    isLoading = false
                )
            }
            return
        }
        viewModelScope.launch(dispatcher) {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        query = trimmedQury,
                        navigateToRecipeId = null
                    )
                }

                val resultados = repository.findRecipeByName(trimmedQury)
                _uiState.update {
                    it.copy(
                        recetasEncontradas = resultados,
                        isLoading = false,
                        errorMessage = if (resultados.isEmpty()) "No se encontraron resultados." else null,
                        navigateToRecipeId = resultados.firstOrNull()?.id
                    )
                }
            } catch (e: Exception){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrio un error al buscar: ${e.message}"
                    )
                }
            }
        }
    }

    fun findRecipeByCategory(categoria: String) = viewModelScope.launch {
        _uiState.update { it
            .copy(isLoading = true, errorMessage = null)
        }

        if (categoria.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La categoría no puede estar vacía.", isLoading = false) }
            return@launch
        }

        try {
            val recetasEncontradas = repository.findByCategory(categoria)

            if (recetasEncontradas.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se encontraron recetas para la categoría: $categoria."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        recetasEncontradas = recetasEncontradas,
                        isLoading = false,
                        navigateToRecipeId = RECIPE_LIST_NAV_ID
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red al buscar categorías.") }
        }
    }
    private val RECIPE_LIST_NAV_ID = 1L

    fun onErrorHandled(){
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
    fun onNavigationHandled(){
        _uiState.update {
            it.copy(navigateToRecipeId = null)
        }
    }
}