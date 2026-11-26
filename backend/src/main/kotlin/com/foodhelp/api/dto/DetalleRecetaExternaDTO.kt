package com.foodhelp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DetalleRecetaExternaDTO(
    val id: Int,
    val title: String,
    val image: String? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
    val extendedIngredients: List<IngredienteExternoDTO> = emptyList(),
    val analyzedInstructions: List<InstruccionesExternasDTO> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IngredienteExternoDTO(
    val name: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val original: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstruccionesExternasDTO(
    val steps: List<PasoExternoDTO> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PasoExternoDTO(
    val step: String? = null,
)
