package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class UsuarioYaExisteException(
    mensaje: String = "El usuario ya se encuentra registrado",
) : RuntimeException(mensaje)
