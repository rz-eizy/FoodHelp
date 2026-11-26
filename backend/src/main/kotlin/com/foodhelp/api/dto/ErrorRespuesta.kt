package com.foodhelp.api.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorRespuesta(
    val error: String,
    val codigo: String,
    val campos: Map<String, String>? = null,
)
