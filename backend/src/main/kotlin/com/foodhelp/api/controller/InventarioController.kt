package com.foodhelp.api.controller

import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.service.InventarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/inventario")
class InventarioController(
    private val inventarioService: InventarioService,
    private val usuarioRepository: UsuarioRepository,
) {
    @GetMapping("/hogar/{hogarId}")
    fun obtenerInventarioPorHogar(
        @PathVariable hogarId: UUID,
    ): ResponseEntity<List<ArticuloDTO>> {
        val inventario = inventarioService.obtenerInventarioPorHogar(hogarId)
        return ResponseEntity.ok(inventario)
    }

    @PatchMapping("/{articuloId}/restar")
    fun restarCantidad(
        @PathVariable articuloId: UUID,
        @RequestParam(required = false) cantidad: Int?,
        @RequestBody(required = false) body: Map<String, Int>?,
    ): ResponseEntity<ArticuloDTO> {
        val cantidadFinal = cantidad ?: body?.get("cantidad") ?: 1
        val articuloActualizado = inventarioService.restarCantidad(articuloId, cantidadFinal)
        return ResponseEntity.ok(articuloActualizado)
    }

    @DeleteMapping("/{articuloId}")
    fun eliminarArticulo(
        @PathVariable articuloId: UUID,
        authentication: Authentication? = null,
    ): ResponseEntity<Void> {
        val auth =
            authentication ?: SecurityContextHolder.getContext().authentication
                ?: throw AccesoDenegadoException("No se encontró información de autenticación")

        val usuario =
            usuarioRepository.findByCorreo(auth.name)
                ?: throw AccesoDenegadoException("Usuario autenticado no encontrado en el sistema")

        val hogarId =
            usuario.hogar?.id
                ?: throw AccesoDenegadoException("El usuario autenticado no está asociado a ningún hogar")

        inventarioService.eliminarArticulo(articuloId, hogarId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(CantidadInsuficienteException::class)
    fun handleCantidadInsuficiente(e: CantidadInsuficienteException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf("error" to (e.message ?: "Operación inválida: saldo negativo")),
        )

    @ExceptionHandler(AccesoDenegadoException::class)
    fun handleAccesoDenegado(e: AccesoDenegadoException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            mapOf("error" to (e.message ?: "Acceso Denegado")),
        )
}
