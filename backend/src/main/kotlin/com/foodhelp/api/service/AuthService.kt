package com.foodhelp.api.service

import com.foodhelp.api.dto.AuthResponse
import com.foodhelp.api.dto.RegistroRequest
import com.foodhelp.api.exception.UsuarioYaExisteException
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun registrar(request: RegistroRequest): AuthResponse {
        if (usuarioRepository.existsByCorreo(request.correo)) {
            throw UsuarioYaExisteException("El correo '${request.correo}' ya se encuentra registrado")
        }

        val contrasenaEncriptada =
            checkNotNull(passwordEncoder.encode(request.contrasena)) {
                "Error al encriptar contraseña"
            }
        val nuevoUsuario =
            Usuario(
                nombre = request.nombre,
                contrasena = contrasenaEncriptada,
                correo = request.correo,
            )

        usuarioRepository.save(nuevoUsuario)

        val token = jwtService.generateToken(request.correo)
        return AuthResponse(token = token)
    }
}
