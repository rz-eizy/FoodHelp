package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class CantidadInsuficienteException(
    mensaje: String = "La resta no puede resultar en un saldo negativo",
    val codigo: CodigoError = CodigoError.CANTIDAD_INSUFICIENTE,
) : RuntimeException(mensaje)
