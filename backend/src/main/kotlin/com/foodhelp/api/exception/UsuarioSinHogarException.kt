package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class UsuarioSinHogarException(
    mensaje: String = "El usuario no pertenece a ningún hogar",
) : RuntimeException(mensaje)
