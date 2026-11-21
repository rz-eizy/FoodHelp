package com.foodhelp.api.controller

import com.foodhelp.api.dto.PerfilResponse
import com.foodhelp.api.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService,
) {
    @GetMapping("/me")
    fun obtenerPerfil(): ResponseEntity<PerfilResponse> = ResponseEntity.ok(usuarioService.perfilActual())

    @PostMapping("/me/logout")
    fun logout(): ResponseEntity<Void> {
        usuarioService.cerrarSesion()
        return ResponseEntity.noContent().build()
    }
}
