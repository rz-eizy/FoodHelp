package com.foodhelp.api.controller

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.HogarResponse
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.service.HogarService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/hogares")
class HogarController(
    private val hogarService: HogarService,
) {
    @PostMapping
    fun crearHogar(
        @Valid @RequestBody request: CreateHogarRequest,
    ): ResponseEntity<HogarResponse> = ResponseEntity.status(HttpStatus.CREATED).body(hogarService.crearHogar(request))

    @PostMapping("/unirse")
    fun unirseAHogar(
        @Valid @RequestBody request: JoinHogarRequest,
    ): ResponseEntity<HogarResponse> = ResponseEntity.ok(hogarService.unirseAHogar(request))

    @GetMapping("/{codigoHogar}")
    fun obtenerHogar(
        @PathVariable codigoHogar: String,
    ): ResponseEntity<HogarResponse> = ResponseEntity.ok(hogarService.obtenerHogar(codigoHogar))

    @PostMapping("/salir")
    fun salirDelHogar(): ResponseEntity<Void> {
        hogarService.salirDelHogar()
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/miembros/{usuarioId}")
    fun expulsarMiembro(
        @PathVariable usuarioId: UUID,
    ): ResponseEntity<Void> {
        hogarService.expulsarMiembro(usuarioId)
        return ResponseEntity.noContent().build()
    }
}
