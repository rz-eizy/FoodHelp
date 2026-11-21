package com.foodhelp.api.security

import com.foodhelp.api.models.Usuario

fun JwtService.tokenParaUsuario(usuario: Usuario): String =
    generateToken(
        usuario.correo,
        mapOf("tv" to usuario.tokenVersion),
    )
