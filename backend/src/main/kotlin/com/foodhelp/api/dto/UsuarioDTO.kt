package com.foodhelp.api.dto

import java.util.UUID

data class HogarResumen(
    val id: UUID,
    val nombre: String,
    val codigoHogar: String,
    val esPropietario: Boolean = false,
)

data class PerfilResponse(
    val id: UUID,
    val nombre: String,
    val correo: String,
    val hogar: HogarResumen?,
)
