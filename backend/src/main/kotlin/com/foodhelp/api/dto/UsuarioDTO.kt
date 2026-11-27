package com.foodhelp.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
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

data class EliminarCuentaRequest(
    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
    val contrasena: String,
)
