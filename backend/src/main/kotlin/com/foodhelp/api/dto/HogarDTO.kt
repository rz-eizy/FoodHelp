package com.foodhelp.api.dto

import java.util.UUID

data class CreateHogarRequest(
    val nombre: String,
)

data class JoinHogarRequest(
    val codigoHogar: String,
)

data class UsuarioResponse(
    val id: UUID,
    val nombre: String,
    // contrasena excluida explícitamente
)

data class HogarResponse(
    val id: UUID,
    val nombre: String,
    val codigoHogar: String,
    val usuarios: List<UsuarioResponse> = emptyList(),
)
