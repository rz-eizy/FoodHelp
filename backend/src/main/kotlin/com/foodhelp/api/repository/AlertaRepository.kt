package com.foodhelp.api.repository

import com.foodhelp.api.models.Alerta
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AlertaRepository : JpaRepository<Alerta, UUID> {
    fun findByHogarIdOrderByFechaCreacionDesc(hogarId: UUID): List<Alerta>

    fun findByArticuloIdIn(articuloIds: Collection<UUID>): List<Alerta>

    fun findByArticuloId(articuloId: UUID): Alerta?

    fun findByHogarId(
        hogarId: UUID,
        pageable: Pageable,
    ): Slice<Alerta>

    fun findByHogarIdAndLeida(
        hogarId: UUID,
        leida: Boolean,
        pageable: Pageable,
    ): Slice<Alerta>

    fun countByHogarIdAndLeidaFalse(hogarId: UUID): Long

    @Modifying
    @Query("update Alerta a set a.leida = true where a.hogarId = :hogarId and a.leida = false")
    fun marcarTodasComoLeidas(
        @Param("hogarId") hogarId: UUID,
    ): Int

    fun deleteByArticuloId(articuloId: UUID)

    fun deleteByHogarId(hogarId: UUID)
}
