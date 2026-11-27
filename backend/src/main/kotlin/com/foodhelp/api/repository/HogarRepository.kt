package com.foodhelp.api.repository

import com.foodhelp.api.models.Hogar
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface HogarRepository : JpaRepository<Hogar, UUID> {
    fun findByCodigoHogar(codigoHogar: String): Hogar?

    fun existsByCodigoHogar(codigoHogar: String): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hogar h where h.id = :id")
    fun buscarParaActualizar(
        @Param("id") id: UUID,
    ): Hogar?
}
