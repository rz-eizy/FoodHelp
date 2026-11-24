package com.foodhelp.api.repository

import com.foodhelp.api.models.Usuario
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun findByCorreo(correo: String): Usuario?

    fun existsByCorreo(correo: String): Boolean

    fun countByHogarId(hogarId: UUID): Long

    fun findFirstByHogarIdAndIdNotOrderByHogarDesdeAscIdAsc(
        hogarId: UUID,
        excluirId: UUID,
    ): Usuario?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.correo = :correo")
    fun buscarParaActualizar(
        @Param("correo") correo: String,
    ): Usuario?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Usuario u set u.tokenVersion = u.tokenVersion + 1 where u.id = :id")
    fun incrementarTokenVersion(
        @Param("id")
        id: UUID,
    ): Int
}
