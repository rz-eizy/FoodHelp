package com.foodhelp.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.NOT_FOUND)
class AlertaNotFoundException(
    id: UUID,
) : RuntimeException("Alerta con ID '$id' no encontrada")
