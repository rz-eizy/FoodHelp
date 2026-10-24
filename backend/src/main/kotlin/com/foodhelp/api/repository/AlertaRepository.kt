package com.foodhelp.api.repository

import com.foodhelp.api.models.Alerta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AlertaRepository : JpaRepository<Alerta, UUID> {
    fun findByHogarIdOrderByFechaCreacionDesc(hogarId: UUID): List<Alerta>
}
