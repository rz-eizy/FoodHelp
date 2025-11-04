package com.example.foodhelp.repository

import com.example.foodhelp.data.Receta
import com.example.foodhelp.data.retrofit.RecetaApiService
import javax.inject.Inject

class RecetaRepositoryImpl @Inject constructor(
    private val apiService: RecetaApiService
) : RecetaRepository {
    override suspend fun findRecipeByName(query: String): List<Receta> {
        return apiService.buscarRecetas(query)
    }
}