package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class ContrasenaIncorrectaException(
    mensaje: String = "Contraseña incorrecta",
    val codigo: CodigoError = CodigoError.CONTRASENA_INCORRECTA,
) : RuntimeException(mensaje)
