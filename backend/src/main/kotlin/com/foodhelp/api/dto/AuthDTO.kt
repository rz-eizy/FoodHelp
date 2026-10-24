package com.foodhelp.api.dto

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
)

data class AuthResponse(
    val token: String,
    val tipo: String = "Bearer",
)
