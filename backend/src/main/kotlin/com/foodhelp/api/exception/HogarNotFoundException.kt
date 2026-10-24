package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class HogarNotFoundException(
    codigoHogar: String,
) : RuntimeException("Hogar con código '$codigoHogar' no encontrado")
