package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class DemasiadosIntentosException(
    mensaje: String = "Demasiados intentos. Inténtalo de nuevo más tarde",
    val codigo: CodigoError = CodigoError.DEMASIADOS_INTENTOS,
) : RuntimeException(mensaje)
