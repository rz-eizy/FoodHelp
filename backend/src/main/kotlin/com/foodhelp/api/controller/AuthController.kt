package com.foodhelp.api.controller

import com.foodhelp.api.dto.AuthResponse
import com.foodhelp.api.dto.RegistroRequest
import com.foodhelp.api.exception.UsuarioYaExisteException
import com.foodhelp.api.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/registro")
    fun registro(
        @RequestBody request: RegistroRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.registrar(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @ExceptionHandler(UsuarioYaExisteException::class)
    fun handleUsuarioYaExiste(e: UsuarioYaExisteException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf("error" to (e.message ?: "Usuario ya existe")),
        )
}
