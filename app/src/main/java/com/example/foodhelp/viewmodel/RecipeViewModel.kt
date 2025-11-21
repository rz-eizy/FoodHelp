package com.example.foodhelp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhelp.repository.RecetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    // Actualiza el estado internamente
    private val _uiState = MutableStateFlow(RecetaUiState())
    // Publico para que pueda ser observado por la UI
    val uiState: StateFlow<RecetaUiState> = _uiState.asStateFlow()

    fun buscarRecetas(query: String) {
        // Implementar mas validaciónes para la busqueda.

        val trimmedQury = query.trim() // Limpiamos espacios Inicio/Final
        if (query.isBlank()){
            _uiState.update {
                it.copy(
                    errorMessage = "La busqueda no puede estar vacia",
                    recetasEncontradas = emptyList(),
                    query = query,
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        query = query,
                        navigateToRecipeId = null
                    )
                }

                val resultados = repository.fin dRecipeByName(query)
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

    fun onNavigationHandled(){
        _uiState.update {
            it.copy(navigateToRecipeId = null)
        }
    }
}