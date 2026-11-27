package com.foodhelp.api.exception

class RecetaNoEncontradaException(
    id: Int,
) : RuntimeException("Receta con ID '$id' no encontrada") {
    val codigo: CodigoError = CodigoError.RECETA_NO_ENCONTRADA
}
