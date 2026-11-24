package com.foodhelp.api.repository

import com.foodhelp.api.models.Alerta
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
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

    fun deleteByArticuloId(articuloId: UUID)
}
