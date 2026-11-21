package com.foodhelp.api.controller

import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.InventarioService
import com.foodhelp.api.service.RecetaIntegrationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/recetas")
class RecetaController(
    private val inventarioService: InventarioService,
    private val recetaIntegrationService: RecetaIntegrationService,
    private val usuarioActualService: UsuarioActualService,
) {
    @GetMapping("/sugerencias")
    fun obtenerSugerencias(): ResponseEntity<Any> {
        val hogarId = usuarioActualService.hogarIdActual()

        val inventario = inventarioService.obtenerInventarioVigente(hogarId)
        if (inventario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to "El inventario del hogar está vacío"),
            )
        }

        val ingredientes =
            inventario
                .sortedBy { it.fechaVencimiento }
                .take(MAX_INGREDIENTES)
                .map { it.nombre }

        return ResponseEntity.ok(recetaIntegrationService.obtenerRecetasPorIngredientes(ingredientes))
    }

    private companion object {
        const val MAX_INGREDIENTES = 3
    }
}
