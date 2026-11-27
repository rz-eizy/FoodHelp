package com.foodhelp.api.controller

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.ActualizarArticuloRequest
import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.dto.CrearArticuloRequest
import com.foodhelp.api.dto.RestarCantidadRequest
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.InventarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/inventario")
class InventarioController(
    private val inventarioService: InventarioService,
    private val usuarioActualService: UsuarioActualService,
) {
    @GetMapping
    fun obtenerMiInventario(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "${Paginacion.TAMANO_POR_DEFECTO}") size: Int,
    ): ResponseEntity<List<ArticuloDTO>> =
        ResponseEntity.ok(
            inventarioService.obtenerInventarioPaginado(usuarioActualService.hogarIdActual(), page, size),
        )

    @GetMapping("/hogar/{hogarId}")
    fun obtenerInventarioPorHogar(
        @PathVariable hogarId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "${Paginacion.TAMANO_POR_DEFECTO}") size: Int,
    ): ResponseEntity<List<ArticuloDTO>> {
        usuarioActualService.exigirPertenenciaAHogar(hogarId)
        return ResponseEntity.ok(inventarioService.obtenerInventarioPaginado(hogarId, page, size))
    }

    @GetMapping("/{articuloId}")
    fun obtenerArticulo(
        @PathVariable articuloId: UUID,
    ): ResponseEntity<ArticuloDTO> =
        ResponseEntity.ok(
            inventarioService.obtenerArticulo(articuloId, usuarioActualService.hogarIdActual()),
        )

    @PostMapping
    fun crearArticulo(
        @Valid @RequestBody request: CrearArticuloRequest,
    ): ResponseEntity<ArticuloDTO> {
        val hogarId = usuarioActualService.hogarIdActual()
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crearArticulo(hogarId, request))
    }

    @PatchMapping("/{articuloId}/restar")
    fun restarCantidad(
        @PathVariable articuloId: UUID,
        @RequestParam(required = false) cantidad: Int?,
        @Valid @RequestBody(required = false) body: RestarCantidadRequest?,
    ): ResponseEntity<ArticuloDTO> {
        val cantidadFinal = cantidad ?: body?.cantidad ?: 1
        val hogarId = usuarioActualService.hogarIdActual()
        return ResponseEntity.ok(inventarioService.restarCantidad(articuloId, hogarId, cantidadFinal))
    }

    @PatchMapping("/{articuloId}")
    fun actualizarArticulo(
        @PathVariable articuloId: UUID,
        @Valid @RequestBody request: ActualizarArticuloRequest,
    ): ResponseEntity<ArticuloDTO> {
        val hogarId = usuarioActualService.hogarIdActual()
        return ResponseEntity.ok(inventarioService.actualizarArticulo(articuloId, hogarId, request))
    }

    @DeleteMapping("/{articuloId}")
    fun eliminarArticulo(
        @PathVariable articuloId: UUID,
    ): ResponseEntity<Void> {
        inventarioService.eliminarArticulo(articuloId, usuarioActualService.hogarIdActual())
        return ResponseEntity.noContent().build()
    }
}
