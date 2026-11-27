package com.foodhelp.api.repository

import com.foodhelp.api.models.HogarExpulsado
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HogarExpulsadoRepository : JpaRepository<HogarExpulsado, UUID> {
    fun existsByHogarIdAndUsuarioId(
        hogarId: UUID,
        usuarioId: UUID,
    ): Boolean

    fun deleteByHogarId(hogarId: UUID)
}
