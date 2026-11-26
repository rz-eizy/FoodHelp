package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.NOT_FOUND)
class ArticuloNotFoundException(
    id: UUID,
    val codigo: CodigoError = CodigoError.ARTICULO_NO_ENCONTRADO,
) : RuntimeException("Artículo con ID '$id' no encontrado")
