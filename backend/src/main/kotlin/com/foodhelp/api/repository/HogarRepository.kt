package com.foodhelp.api.repository

import com.foodhelp.api.models.Hogar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface HogarRepository : JpaRepository<Hogar, UUID> {
    fun findByCodigoHogar(codigoHogar: String): Hogar?
}
