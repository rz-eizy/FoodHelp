package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class SolicitudInvalidaException(
    mensaje: String,
    val codigo: CodigoError = CodigoError.SOLICITUD_INVALIDA,
) : RuntimeException(mensaje)
