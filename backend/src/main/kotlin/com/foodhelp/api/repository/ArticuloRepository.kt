package com.foodhelp.api.repository

import com.foodhelp.api.models.ArticuloDespensa
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface ArticuloRepository : JpaRepository<ArticuloDespensa, UUID> {
    fun findByHogarId(hogarId: UUID): List<ArticuloDespensa>

    fun findByFechaVencimientoBetween(
        fechaInicio: LocalDate,
        fechaFin: LocalDate,
    ): List<ArticuloDespensa>
}
