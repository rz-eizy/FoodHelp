package com.foodhelp.api.controller

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.AlertaResponse
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.AlertaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/alertas")
class AlertaController(
    private val alertaService: AlertaService,
    private val usuarioActualService: UsuarioActualService,
) {
    @GetMapping
    fun obtenerAlertasDelHogar(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "${Paginacion.TAMANO_POR_DEFECTO}") size: Int,
        @RequestParam(required = false) leida: Boolean?,
    ): ResponseEntity<List<AlertaResponse>> {
        val hogarId = usuarioActualService.hogarIdActual()
        val alertas =
            if (leida == null) {
                alertaService.listarPorHogar(hogarId, page, size)
            } else {
                alertaService.listarPorHogar(hogarId, page, size, leida)
            }
        return ResponseEntity.ok(alertas)
    }

    @GetMapping("/hogar/{hogarId}")
    fun obtenerAlertasPorHogarId(
        @PathVariable hogarId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "${Paginacion.TAMANO_POR_DEFECTO}") size: Int,
    ): ResponseEntity<List<AlertaResponse>> {
        usuarioActualService.exigirPertenenciaAHogar(hogarId)
        return ResponseEntity.ok(alertaService.listarPorHogar(hogarId, page, size))
    }

    @PatchMapping("/{id}/leida")
    fun marcarComoLeida(
        @PathVariable id: UUID,
    ): ResponseEntity<AlertaResponse> = ResponseEntity.ok(alertaService.marcarComoLeida(id, usuarioActualService.hogarIdActual()))
}
