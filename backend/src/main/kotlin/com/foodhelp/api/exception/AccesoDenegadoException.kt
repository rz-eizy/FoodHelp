package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class AccesoDenegadoException(
    mensaje: String = "Acceso Denegado: el recurso no pertenece al hogar del usuario",
) : RuntimeException(mensaje)
