package com.foodhelp.api.service

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.HogarResponse
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.dto.UsuarioResponse
import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.HogarNotFoundException
import com.foodhelp.api.exception.MiembroNotFoundException
import com.foodhelp.api.exception.SolicitudInvalidaException
import com.foodhelp.api.exception.UsuarioSinHogarException
import com.foodhelp.api.exception.UsuarioYaEnHogarException
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.HogarExpulsado
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.HogarExpulsadoRepository
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.LimitadorIntentos
import com.foodhelp.api.security.UsuarioActualService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class HogarService(
    private val hogarRepository: HogarRepository,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioActualService: UsuarioActualService,
    @Qualifier("limitadorUnirse")
    private val limitadorUnirse: LimitadorIntentos,
    private val clock: Clock,
    private val alertaRepository: AlertaRepository,
    private val hogarExpulsadoRepository: HogarExpulsadoRepository,
) {
    @Transactional
    fun salirDelHogar() {
        retirarMiembro(usuarioActualService.usuarioActual())
    }

    @Transactional
    fun expulsarMiembro(usuarioId: UUID) {
        val actorInicial = usuarioActualService.usuarioActual()
        if (actorInicial.hogar == null) throw UsuarioSinHogarException()
        val (hogar, actor) = hogarYUsuarioBloqueados(actorInicial)
        if (hogar.propietarioId != actor.id) {
            throw AccesoDenegadoException("Solo el propietario del hogar puede expulsar integrantes")
        }
        if (actor.id == usuarioId) {
            throw SolicitudInvalidaException("Para abandonar el hogar usa la opción de salir")
        }

        val objetivo = usuarioRepository.findById(usuarioId).orElseThrow { MiembroNotFoundException() }
        if (objetivo.hogar?.id != hogar.id) throw MiembroNotFoundException()

        val hogarId = checkNotNull(hogar.id)
        objetivo.hogar = null
        objetivo.hogarDesde = null
        hogar.usuarios.remove(objetivo)
        usuarioRepository.saveAndFlush(objetivo)
        hogarExpulsadoRepository.save(
            HogarExpulsado(
                hogarId = hogarId,
                usuarioId = usuarioId,
                expulsadoEn = Instant.now(clock),
            ),
        )
    }

    // Orden fijo de bloqueo (hogar -> usuario) para evitar interbloqueos entre operaciones concurrentes
    private fun hogarYUsuarioBloqueados(usuarioInicial: Usuario): Pair<Hogar, Usuario> {
        val hogarId = checkNotNull(usuarioInicial.hogar?.id)
        val hogar =
            hogarRepository.buscarParaActualizar(hogarId)
                ?: throw HogarNotFoundException(hogarId.toString())
        val usuario =
            usuarioRepository.buscarParaActualizar(usuarioInicial.correo)
                ?: throw UsuarioSinHogarException()
        if (usuario.hogar?.id != hogar.id) {
            if (usuario.hogar == null) throw UsuarioSinHogarException()
            throw UsuarioYaEnHogarException()
        }
        return hogar to usuario
    }

    // Bloquea en orden fijo hogar -> usuario y devuelve la entidad bloqueada para que
    // eliminar cuenta (PR18) pueda continuar con la anonimizacion en la misma transaccion
    internal fun retirarMiembro(usuarioInicial: Usuario): Usuario {
        if (usuarioInicial.hogar == null) throw UsuarioSinHogarException()
        val (hogar, usuario) = hogarYUsuarioBloqueados(usuarioInicial)
        retirarMiembroBloqueado(usuario, hogar)
        return usuario
    }

    private fun retirarMiembroBloqueado(
        usuario: Usuario,
        hogar: Hogar,
    ) {
        val hogarId = checkNotNull(hogar.id)
        val usuarioId = checkNotNull(usuario.id)

        if (hogar.propietarioId == usuarioId) {
            hogar.propietarioId =
                usuarioRepository.findFirstByHogarIdAndIdNotOrderByHogarDesdeAscIdAsc(hogarId, usuarioId)?.id
        }

        usuario.hogar = null
        usuario.hogarDesde = null
        hogar.usuarios.remove(usuario)
        usuarioRepository.saveAndFlush(usuario)

        if (usuarioRepository.countByHogarId(hogarId) != 0L) return

        alertaRepository.deleteByHogarId(hogarId)
        hogarExpulsadoRepository.deleteByHogarId(hogarId)
        hogarRepository.delete(hogar)
    }

    @Transactional
    fun crearHogar(request: CreateHogarRequest): HogarResponse {
        val usuarioInicial = usuarioActualService.usuarioActual()
        if (usuarioInicial.hogar != null) throw UsuarioYaEnHogarException()
        val usuario =
            usuarioRepository.buscarParaActualizar(usuarioInicial.correo)
                ?: throw UsuarioSinHogarException()
        if (usuario.hogar != null) throw UsuarioYaEnHogarException()

        val hogar =
            hogarRepository.save(
                Hogar(
                    nombre = request.nombre.trim(),
                    codigoHogar = generarCodigoUnico(),
                    propietarioId = checkNotNull(usuario.id),
                ),
            )

        // El alta del creador se persiste por dirty checking, sin guardado explícito
        usuario.hogar = hogar
        usuario.hogarDesde = Instant.now(clock)
        hogar.usuarios.add(usuario)
        return hogar.toResponse(usuario.id)
    }

    @Transactional
    fun unirseAHogar(request: JoinHogarRequest): HogarResponse {
        val usuarioInicial = usuarioActualService.usuarioActual()
        if (usuarioInicial.hogar != null) throw UsuarioYaEnHogarException()

        val clave = usuarioInicial.correo
        limitadorUnirse.exigirDisponible(clave)

        val codigo = request.codigoHogar.trim().uppercase()
        val hogar =
            try {
                hogarRepository.findByCodigoHogar(codigo)
                    ?: throw HogarNotFoundException(codigo)
            } catch (e: HogarNotFoundException) {
                limitadorUnirse.registrarFallo(clave)
                throw e
            }

        val hogarBloqueado =
            hogarRepository.buscarParaActualizar(checkNotNull(hogar.id))
                ?: throw HogarNotFoundException(codigo)
        val usuario = usuarioRepository.buscarParaActualizar(clave) ?: throw UsuarioSinHogarException()
        if (usuario.hogar != null) throw UsuarioYaEnHogarException()

        val hogarId = checkNotNull(hogarBloqueado.id)
        if (hogarExpulsadoRepository.existsByHogarIdAndUsuarioId(hogarId, checkNotNull(usuario.id))) {
            throw AccesoDenegadoException("No puedes unirte a este hogar")
        }

        usuario.hogar = hogarBloqueado
        usuario.hogarDesde = Instant.now(clock)
        hogarBloqueado.usuarios.add(usuario)
        if (hogarBloqueado.propietarioId == null) {
            hogarBloqueado.propietarioId = usuario.id
        }
        limitadorUnirse.reiniciar(clave)
        return hogarBloqueado.toResponse(usuario.id)
    }

    @Transactional(readOnly = true)
    fun obtenerHogar(codigoHogar: String): HogarResponse {
        val codigo = codigoHogar.trim().uppercase()
        val hogar =
            hogarRepository.findByCodigoHogar(codigo)
                ?: throw HogarNotFoundException(codigoHogar)

        // Responder 404 también a quien no es miembro, igual que si el código no existiera,
        // evita revelar qué códigos de hogar son válidos
        val usuario = usuarioActualService.usuarioActual()
        if (usuario.hogar?.id != hogar.id) {
            throw HogarNotFoundException(codigoHogar)
        }
        return hogar.toResponse(usuario.id)
    }

    // 10 caracteres de un alfabeto de 32 símbolos dan 50 bits de entropía
    private fun generarCodigoUnico(): String {
        repeat(MAX_INTENTOS_CODIGO) {
            val codigo =
                CharArray(LONGITUD_CODIGO) { ALFABETO[random.nextInt(ALFABETO.length)] }
                    .concatToString()
            if (!hogarRepository.existsByCodigoHogar(codigo)) return codigo
        }
        error("No fue posible generar un código de hogar único")
    }

    private fun Hogar.toResponse(usuarioId: UUID?): HogarResponse =
        HogarResponse(
            id = checkNotNull(id),
            nombre = nombre,
            codigoHogar = codigoHogar,
            usuarios =
                usuarios
                    .distinctBy { it.id }
                    .map {
                        UsuarioResponse(
                            id = checkNotNull(it.id),
                            nombre = it.nombre,
                            esPropietario = it.id == propietarioId,
                        )
                    },
            esPropietario = usuarioId == propietarioId,
        )

    private companion object {
        const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // sin 0/O/1/I, que se confunden al transcribir
        const val LONGITUD_CODIGO = 10
        const val MAX_INTENTOS_CODIGO = 5
        val random = SecureRandom()
    }
}
