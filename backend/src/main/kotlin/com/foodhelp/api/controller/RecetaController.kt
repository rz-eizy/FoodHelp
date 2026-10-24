package com.foodhelp.api.controller

import com.foodhelp.api.dto.RecetaExternaDTO
import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.service.InventarioService
import com.foodhelp.api.service.RecetaIntegrationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/recetas")
class RecetaController(
    private val inventarioService: InventarioService,
    private val recetaIntegrationService: RecetaIntegrationService,
    private val usuarioRepository: UsuarioRepository,
) {
    @GetMapping("/sugerencias")
    fun obtenerSugerencias(authentication: Authentication? = null): ResponseEntity<Any> {
        val auth =
            authentication ?: SecurityContextHolder.getContext().authentication
                ?: throw AccesoDenegadoException("No se encontró información de autenticación")

        val usuario =
            usuarioRepository.findByCorreo(auth.name)
                ?: throw AccesoDenegadoException("Usuario autenticado no encontrado en el sistema")

        val hogarId =
            usuario.hogar?.id
                ?: throw AccesoDenegadoException("El usuario autenticado no está asociado a ningún hogar")

        val inventario = inventarioService.obtenerInventarioPorHogar(hogarId)
        if (inventario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to "El inventario del hogar está vacío"),
            )
        }

        val ingredientes =
            inventario
                .sortedBy { it.fechaVencimiento }
                .take(3)
                .map { it.nombre }

        val recetas: List<RecetaExternaDTO> = recetaIntegrationService.obtenerRecetasPorIngredientes(ingredientes)
        return ResponseEntity.ok(recetas)
    }

    @ExceptionHandler(AccesoDenegadoException::class)
    fun handleAccesoDenegado(e: AccesoDenegadoException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            mapOf("error" to (e.message ?: "Acceso Denegado")),
        )
}
