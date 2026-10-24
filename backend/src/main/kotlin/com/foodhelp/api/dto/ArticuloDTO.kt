package com.foodhelp.api.dto

import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import java.time.LocalDate
import java.util.UUID

data class ArticuloDTO(
    val id: UUID,
    val nombre: String,
    val fechaVencimiento: LocalDate,
    val cantidad: Int,
    val categoria: CategoriaEnum,
    val hogarId: UUID,
) {
    companion object {
        fun fromEntity(entity: ArticuloDespensa): ArticuloDTO =
            ArticuloDTO(
                id = entity.id!!,
                nombre = entity.nombre,
                fechaVencimiento = entity.fechaVencimiento!!,
                cantidad = entity.cantidad,
                categoria = entity.categoria,
                hogarId = entity.hogar.id!!,
            )
    }
}
