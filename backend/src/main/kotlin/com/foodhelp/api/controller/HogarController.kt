package com.foodhelp.api.controller

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.HogarResponse
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.service.HogarService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/hogares")
class HogarController(
    private val hogarService: HogarService,
) {
    @PostMapping
    fun crearHogar(
        @RequestBody request: CreateHogarRequest,
    ): ResponseEntity<HogarResponse> {
        val response = hogarService.crearHogar(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/unirse")
    fun unirseAHogar(
        @RequestBody request: JoinHogarRequest,
    ): ResponseEntity<HogarResponse> {
        val response = hogarService.unirseAHogar(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{codigoHogar}")
    fun obtenerHogar(
        @PathVariable codigoHogar: String,
    ): ResponseEntity<HogarResponse> {
        val response = hogarService.obtenerHogar(codigoHogar)
        return ResponseEntity.ok(response)
    }
}
