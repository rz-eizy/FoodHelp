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

    @GET("api/recetas/por-categoria")
    suspend fun buscarRecetasPorCategoria(
        @Query("categoria") nombreCategoria: String
    ): List<Receta>


    @GET("api/recetas/por-ingrediente/or")
    suspend fun buscarPorCualquierIngrediente(
        // se le enviar una lista de ingredientes y te devuelve una lista
        // de recetas que contengan al menos uno de los ingredientes
        @Query("ingredientes") ingredientes: List<String>
    ): List<Receta>

    @GET("api/recetas/por-ingrediente/and")
    suspend fun buscarPorTodosLosIngredientes(
        @Query("ingredientes") ingredientes: List<String>
    ): List<Receta>
}