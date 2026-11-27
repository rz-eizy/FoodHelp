package com.foodhelp.api.controller

import com.foodhelp.api.dto.RecetaDetalleDTO
import com.foodhelp.api.exception.CodigoError
import com.foodhelp.api.exception.RecetaNoEncontradaException
import com.foodhelp.api.exception.SolicitudInvalidaException
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.InventarioService
import com.foodhelp.api.service.RecetaIntegrationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
            throw SolicitudInvalidaException(
                "El inventario del hogar está vacío",
                CodigoError.INVENTARIO_VACIO,
            )
        }

        val ingredientes =
            inventario
                .sortedBy { it.fechaVencimiento }
                .take(MAX_INGREDIENTES)
                .map { it.nombreIngles ?: it.nombre }

        return ResponseEntity.ok(recetaIntegrationService.obtenerRecetasPorIngredientes(ingredientes))
    }

    @GetMapping("/{id}")
    fun obtenerDetalle(
        @PathVariable id: Int,
    ): RecetaDetalleDTO {
        if (id < 1) throw SolicitudInvalidaException("El ID de la receta debe ser mayor que cero")
        usuarioActualService.hogarIdActual()
        return recetaIntegrationService.obtenerDetalleReceta(id) ?: throw RecetaNoEncontradaException(id)
    }

    private companion object {
        const val MAX_INGREDIENTES = 3
    }
}
