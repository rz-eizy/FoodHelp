package com.example.foodhelp.repository

import com.example.foodhelp.data.Receta
import com.example.foodhelp.data.retrofit.RecetaApiService
import retrofit2.Response
import javax.inject.Inject

class RecetaRepositoryImpl @Inject constructor(
    private val apiService: RecetaApiService
) : RecetaRepository {
    override suspend fun findRecipeByName(query: String): List<Receta> {
        return apiService.buscarRecetas(query)
    }

    override suspend fun findByCategory(query: String): List<Receta> {
        return apiService.buscarRecetasPorCategoria(query)
    }

    override suspend fun finById(id: Long): Receta? {
        return apiService.buscarRecetaPorId((id))
    }
}