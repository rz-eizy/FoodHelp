package com.foodhelp.api.dto

data class RecetaDetalleDTO(
    val id: Int,
    val title: String,
    val image: String? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
    val ingredients: List<IngredienteRecetaDTO> = emptyList(),
    val steps: List<PasoRecetaDTO> = emptyList(),
)

data class IngredienteRecetaDTO(
    val name: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val original: String? = null,
)

data class PasoRecetaDTO(
    val number: Int,
    val step: String,
)
