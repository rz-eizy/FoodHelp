package com.foodhelp.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegistroRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    val nombre: String,
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no es válido")
    @field:Size(max = 254, message = "El correo no puede superar 254 caracteres")
    val correo: String,
    @field:Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    val contrasena: String,
)

data class LoginRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no es válido")
    val correo: String,
    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(max = 72)
    val contrasena: String,
)

data class AuthResponse(
    val token: String,
    val tipo: String = "Bearer",
)
