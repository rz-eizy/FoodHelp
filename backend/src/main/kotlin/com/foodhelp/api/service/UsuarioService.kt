package com.foodhelp.api.service

import com.foodhelp.api.dto.EliminarCuentaRequest
import com.foodhelp.api.dto.HogarResumen
import com.foodhelp.api.dto.PerfilResponse
import com.foodhelp.api.exception.ContrasenaIncorrectaException
import com.foodhelp.api.exception.UsuarioSinHogarException
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.LimitadorIntentos
import com.foodhelp.api.security.UsuarioActualService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class UsuarioService(
    private val usuarioActualService: UsuarioActualService,
    private val usuarioRepository: UsuarioRepository,
    private val hogarService: HogarService,
    private val passwordEncoder: PasswordEncoder,
    private val clock: Clock,
    @Qualifier("limitadorLogin")
    private val limitadorLogin: LimitadorIntentos,
) {
    @Transactional
    fun cerrarSesion() {
        val usuario = usuarioActualService.usuarioActual()
        usuarioRepository.incrementarTokenVersion(checkNotNull(usuario.id))
    }

    @Transactional
    fun eliminarCuenta(request: EliminarCuentaRequest) {
        val usuarioInicial = usuarioActualService.usuarioActual()
        val correo = usuarioInicial.correo
        limitadorLogin.exigirDisponible(correo)

        if (!passwordEncoder.matches(request.contrasena, usuarioInicial.contrasena)) {
            limitadorLogin.registrarFallo(correo)
            throw ContrasenaIncorrectaException()
        }
        limitadorLogin.reiniciar(correo)

        val usuario =
            if (usuarioInicial.hogar != null) {
                hogarService.retirarMiembro(usuarioInicial)
            } else {
                usuarioRepository.buscarParaActualizar(correo) ?: throw UsuarioSinHogarException()
            }

        val id = checkNotNull(usuario.id)
        usuario.tokenVersion++
        usuario.correo = "eliminado-$id@cuenta-eliminada.invalid"
        usuario.nombre = "Usuario eliminado"
        usuario.contrasena = checkNotNull(passwordEncoder.encode(UUID.randomUUID().toString()))
        usuario.hogar = null
        usuario.hogarDesde = null
        usuario.eliminadoEn = Instant.now(clock)
        usuarioRepository.saveAndFlush(usuario)
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
                    esPropietario = usuario.id == it.propietarioId,
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
