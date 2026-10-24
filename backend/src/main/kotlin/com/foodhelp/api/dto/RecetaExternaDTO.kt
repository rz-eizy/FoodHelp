package com.foodhelp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecetaExternaDTO(
    val id: Int,
    val title: String,
    val image: String,
    val missedIngredientCount: Int,
)
