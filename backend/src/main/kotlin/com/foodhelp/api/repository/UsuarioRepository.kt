package com.foodhelp.api.repository

import com.foodhelp.api.models.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun findByCorreo(correo: String): Usuario?

    fun existsByCorreo(correo: String): Boolean
}
