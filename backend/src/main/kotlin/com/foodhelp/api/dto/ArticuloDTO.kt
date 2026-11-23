package com.foodhelp.api.dto

import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
                id = checkNotNull(entity.id) { "El artículo no está persistido (id es null)" },
                nombre = entity.nombre,
                fechaVencimiento =
                    checkNotNull(entity.fechaVencimiento) {
                        "El artículo '${entity.nombre}' no tiene fecha de vencimiento"
                    },
                cantidad = entity.cantidad,
                categoria = entity.categoria,
                hogarId = checkNotNull(entity.hogar.id) { "El hogar del artículo '${entity.nombre}' no está persistido" },
            )
    }
}

data class CrearArticuloRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    val nombre: String,
    @field:NotNull(message = "La fecha de vencimiento es obligatoria")
    val fechaVencimiento: LocalDate?,
    @field:Min(value = 1, message = "La cantidad debe ser al menos 1")
    val cantidad: Int,
    val categoria: CategoriaEnum = CategoriaEnum.OTROS,
)

data class RestarCantidadRequest(
    @field:Min(value = 1, message = "La cantidad a restar debe ser al menos 1")
    val cantidad: Int = 1,
)

data class ActualizarArticuloRequest(
    @field:Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    @field:Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar en blanco")
    val nombre: String? = null,
    val fechaVencimiento: LocalDate? = null,
    @field:Min(value = 0, message = "La cantidad no puede ser negativa")
    val cantidad: Int? = null,
    val categoria: CategoriaEnum? = null,
)
