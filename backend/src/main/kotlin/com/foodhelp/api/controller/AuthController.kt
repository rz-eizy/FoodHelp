package com.foodhelp.api.controller

import com.foodhelp.api.dto.AuthResponse
import com.foodhelp.api.dto.LoginRequest
import com.foodhelp.api.dto.RegistroRequest
import com.foodhelp.api.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        @Valid @RequestBody request: RegistroRequest,
    ): ResponseEntity<AuthResponse> = ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request))

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> = ResponseEntity.ok(authService.login(request))
}
