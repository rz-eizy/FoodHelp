package com.foodhelp.api.repository

import com.foodhelp.api.models.ArticuloDespensa
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface ArticuloRepository : JpaRepository<ArticuloDespensa, UUID> {
    fun findByHogarId(hogarId: UUID): List<ArticuloDespensa>

    fun findByHogarId(
        hogarId: UUID,
        pageable: Pageable,
    ): Slice<ArticuloDespensa>

    @Query(
        "select a from ArticuloDespensa a join fetch a.hogar " +
            "where a.fechaVencimiento <= :hasta and a.cantidad > 0",
    )
    fun findConAlertaActiva(
        @Param("hasta") hasta: LocalDate,
    ): List<ArticuloDespensa>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ArticuloDespensa a set a.cantidad = a.cantidad - :n where a.id = :id and a.cantidad >= :n")
    fun restarCantidadAtomica(
        @Param("id") id: UUID,
        @Param("n") n: Int,
    ): Int
}
