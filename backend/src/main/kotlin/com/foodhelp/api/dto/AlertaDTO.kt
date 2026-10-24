package com.foodhelp.api.dto

import java.util.UUID

data class AlertaDTO(
    val articuloId: UUID,
    val nombreProducto: String,
    val diasRestantes: Long,
    val hogarId: UUID,
)
