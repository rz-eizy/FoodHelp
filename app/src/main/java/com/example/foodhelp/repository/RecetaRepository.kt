package com.example.foodhelp.repository

import com.example.foodhelp.data.Receta
interface RecetaRepository {
    suspend fun findRecipeByName(query: String): List<Receta>
    suspend fun findByCategory(query: String): List<Receta>
}