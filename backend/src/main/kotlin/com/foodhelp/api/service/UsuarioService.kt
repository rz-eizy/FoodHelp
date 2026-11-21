package com.foodhelp.api.service

import com.foodhelp.api.dto.HogarResumen
import com.foodhelp.api.dto.PerfilResponse
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.UsuarioActualService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService(
    private val usuarioActualService: UsuarioActualService,
    private val usuarioRepository: UsuarioRepository,
) {
    @Transactional
    fun cerrarSesion() {
        val usuario = usuarioActualService.usuarioActual()
        usuarioRepository.incrementarTokenVersion(checkNotNull(usuario.id))
    }

    @Transactional(readOnly = true)
    fun perfilActual(): PerfilResponse {
        val usuario = usuarioActualService.usuarioActual()
        val hogarResumen =
            usuario.hogar?.let {
                HogarResumen(
                    id = checkNotNull(it.id),
                    nombre = it.nombre,
                    codigoHogar = it.codigoHogar,
                )
            }
        return PerfilResponse(
            id = checkNotNull(usuario.id),
            nombre = usuario.nombre,
            correo = usuario.correo,
            hogar = hogarResumen,
        )
    }
}
