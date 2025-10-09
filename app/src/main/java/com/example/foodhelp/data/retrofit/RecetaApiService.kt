package com.example.foodhelp.data.retrofit

import com.example.foodhelp.data.Receta
import retrofit2.http.GET
import retrofit2.http.Query

// clase encargada de hacer las llamadas a springbot

interface RecetaApiService {

    @GET("api/recetas/buscar")
    suspend fun buscarRecetas(
        @Query("nombre") query: String
    ): List<Receta>

    @GET("api/recetas")
    suspend fun obtenerTodasLasRecetas(): List<Receta>
}