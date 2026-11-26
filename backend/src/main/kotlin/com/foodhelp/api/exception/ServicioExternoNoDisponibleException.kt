package com.foodhelp.api.exception

class ServicioExternoNoDisponibleException : RuntimeException("Servicio externo no disponible") {
    val codigo: CodigoError = CodigoError.SERVICIO_EXTERNO_NO_DISPONIBLE
}
