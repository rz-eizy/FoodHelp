package com.foodhelp.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateHogarRequest(
    @field:NotBlank(message = "El nombre del hogar es obligatorio")
    @field:Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    val nombre: String,
)

data class JoinHogarRequest(
    @field:NotBlank(message = "El código de invitación es obligatorio")
    @field:Size(max = 32, message = "El código de invitación no es válido")
    val codigoHogar: String,
)

data class UsuarioResponse(
    val id: UUID,
    val nombre: String,
    val esPropietario: Boolean = false,
)

data class HogarResponse(
    val id: UUID,
    val nombre: String,
    val codigoHogar: String,
    val usuarios: List<UsuarioResponse> = emptyList(),
    val esPropietario: Boolean = false,
)
