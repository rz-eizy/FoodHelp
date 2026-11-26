package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class CredencialesInvalidasException(
    mensaje: String = "Credenciales inválidas",
    val codigo: CodigoError = CodigoError.CREDENCIALES_INVALIDAS,
) : RuntimeException(mensaje)
