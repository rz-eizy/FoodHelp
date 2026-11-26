package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class UsuarioYaEnHogarException(
    mensaje: String = "El usuario ya pertenece a un hogar",
    val codigo: CodigoError = CodigoError.USUARIO_YA_EN_HOGAR,
) : RuntimeException(mensaje)
