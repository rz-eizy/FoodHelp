package com.foodhelp.api.config

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

object Paginacion {
    const val TAMANO_POR_DEFECTO = 50
    const val TAMANO_MAXIMO = 100

    fun de(
        page: Int,
        size: Int,
        orden: Sort,
    ): Pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, TAMANO_MAXIMO), orden)
}
