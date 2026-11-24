package com.foodhelp.api.dto

import java.time.LocalDate
import java.util.UUID

data class AlertaDTO(
    val articuloId: UUID,
    val nombreProducto: String,
    val diasRestantes: Long,
    val hogarId: UUID,
)

data class AlertaResponse(
    val id: UUID,
    val articuloId: UUID,
    val nombreProducto: String,
    val diasRestantes: Int,
    val leida: Boolean,
    val fechaCreacion: LocalDate,
    val vencido: Boolean = diasRestantes < 0,
)
