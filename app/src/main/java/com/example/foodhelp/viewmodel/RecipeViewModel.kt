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
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()){
            _uiState.update {
                it.copy(
                    errorMessage = "La busqueda no puede estar vacia",
                    recetas = emptyList(),
                    query = trimmedQuery,
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
                        query = trimmedQuery,
                        navigateToRecipeId = null
                    )
                }

                val resultados = repository.findRecipeByName(trimmedQuery)
                _uiState.update {
                    it.copy(
                        recetas = resultados,
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
            .copy(isLoading = true, errorMessage = null, navigateToRecipeId = null)
        }

        if (categoria.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La categoría no puede estar vacía.", isLoading = false) }
            return@launch
        }

        try {
            val recetas = repository.findByCategory(categoria)
            if (recetas.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se encontraron recetas para la categoría: $categoria.",
                        recetas = emptyList()
                    )
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        recetas = recetas,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}.") }
        }
    }

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