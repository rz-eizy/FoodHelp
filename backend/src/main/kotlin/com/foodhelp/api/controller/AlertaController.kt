package com.foodhelp.api.controller

import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/alertas")
class AlertaController(
    private val alertaRepository: AlertaRepository,
    private val usuarioRepository: UsuarioRepository,
) {
    @GetMapping
    fun obtenerAlertasDelHogar(authentication: Authentication? = null): ResponseEntity<List<Alerta>> {
        val auth =
            authentication ?: SecurityContextHolder.getContext().authentication
                ?: throw AccesoDenegadoException("No se encontró información de autenticación")

        val usuario =
            usuarioRepository.findByCorreo(auth.name)
                ?: throw AccesoDenegadoException("Usuario autenticado no encontrado en el sistema")

        val hogarId =
            usuario.hogar?.id
                ?: throw AccesoDenegadoException("El usuario autenticado no está asociado a ningún hogar")

        val alertas = alertaRepository.findByHogarIdOrderByFechaCreacionDesc(hogarId)
        return ResponseEntity.ok(alertas)
    }

    @GetMapping("/hogar/{hogarId}")
    fun obtenerAlertasPorHogarId(
        @PathVariable hogarId: UUID,
    ): ResponseEntity<List<Alerta>> {
        val alertas = alertaRepository.findByHogarIdOrderByFechaCreacionDesc(hogarId)
        return ResponseEntity.ok(alertas)
    }

    @PatchMapping("/{id}/leida")
    fun marcarComoLeida(
        @PathVariable id: UUID,
    ): ResponseEntity<Alerta> {
        val alerta =
            alertaRepository
                .findById(id)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada con ID: $id") }

        alerta.leida = true
        val alertaActualizada = alertaRepository.save(alerta)
        return ResponseEntity.ok(alertaActualizada)
    }

    @ExceptionHandler(AccesoDenegadoException::class)
    fun handleAccesoDenegado(e: AccesoDenegadoException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            mapOf("error" to (e.message ?: "Acceso Denegado")),
        )
}
