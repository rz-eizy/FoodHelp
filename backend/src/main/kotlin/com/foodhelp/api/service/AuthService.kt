package com.foodhelp.api.service

import com.foodhelp.api.dto.AuthResponse
import com.foodhelp.api.dto.LoginRequest
import com.foodhelp.api.dto.RegistroRequest
import com.foodhelp.api.exception.CredencialesInvalidasException
import com.foodhelp.api.exception.UsuarioYaExisteException
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.LimitadorIntentos
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    @Qualifier("limitadorLogin")
    private val limitadorLogin: LimitadorIntentos,
) {
    // Hash de relleno para igualar el coste de login() aunque el correo no exista (anti-enumeración)
    private val hashFicticio: String by lazy {
        checkNotNull(passwordEncoder.encode("contrasena-ficticia-sin-uso"))
    }

    @Transactional
    fun registrar(request: RegistroRequest): AuthResponse {
        val correo = normalizar(request.correo)
        if (usuarioRepository.existsByCorreo(correo)) {
            throw UsuarioYaExisteException("El correo '$correo' ya se encuentra registrado")
        }

        val contrasenaEncriptada =
            checkNotNull(passwordEncoder.encode(request.contrasena)) {
                "Error al encriptar contraseña"
            }

        val usuarioGuardado =
            usuarioRepository.save(
                Usuario(nombre = request.nombre.trim(), contrasena = contrasenaEncriptada, correo = correo),
            )

        return AuthResponse(token = jwtService.generateToken(correo, mapOf("tv" to usuarioGuardado.tokenVersion)))
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val correoNormalizado = normalizar(request.correo)
        limitadorLogin.exigirDisponible(correoNormalizado)

        val usuario = usuarioRepository.findByCorreo(correoNormalizado)
        val coincide = passwordEncoder.matches(request.contrasena, usuario?.contrasena ?: hashFicticio)

        if (usuario == null || !coincide) {
            limitadorLogin.registrarFallo(correoNormalizado)
            throw CredencialesInvalidasException()
        }

        limitadorLogin.reiniciar(correoNormalizado)
        return AuthResponse(token = jwtService.generateToken(usuario.correo, mapOf("tv" to usuario.tokenVersion)))
    }

    private fun normalizar(correo: String): String = correo.trim().lowercase()
}
