package com.foodhelp.api.controller

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.AlertaResponse
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.AlertaService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(AlertaController::class)
@AutoConfigureMockMvc(addFilters = false)
class AlertaControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var alertaService: AlertaService

    @MockitoBean
    private lateinit var usuarioActualService: UsuarioActualService

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var usuarioRepository: UsuarioRepository

    private val hogarId = UUID.randomUUID()

    @Test
    @WithMockUser
    fun `Test 1 - recuperacion de la lista de alertas del hogar con HTTP 200`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)

        val alerta1 =
            AlertaResponse(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Leche Entera",
                diasRestantes = 2,
                leida = false,
                fechaCreacion = LocalDate.now(),
            )
        val alerta2 =
            AlertaResponse(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Yogur de Frutilla",
                diasRestantes = 5,
                leida = true,
                fechaCreacion = LocalDate.now().minusDays(1),
            )

        `when`(alertaService.listarPorHogar(hogarId, 0, Paginacion.TAMANO_POR_DEFECTO))
            .thenReturn(listOf(alerta1, alerta2))

        mockMvc
            .perform(
                get("/api/alertas")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].nombreProducto").value("Leche Entera"))
            .andExpect(jsonPath("$[0].diasRestantes").value(2))
            .andExpect(jsonPath("$[0].leida").value(false))
            .andExpect(jsonPath("$[1].nombreProducto").value("Yogur de Frutilla"))
            .andExpect(jsonPath("$[1].leida").value(true))
    }

    @Test
    @WithMockUser
    fun `Test 2 - actualizacion del estado de lectura con PATCH a leida true`() {
        val alertaId = UUID.randomUUID()
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)

        val alertaModificada =
            AlertaResponse(
                id = alertaId,
                articuloId = UUID.randomUUID(),
                nombreProducto = "Pan Lactal",
                diasRestantes = 1,
                leida = true,
                fechaCreacion = LocalDate.now(),
            )

        `when`(alertaService.marcarComoLeida(alertaId, hogarId)).thenReturn(alertaModificada)

        mockMvc
            .perform(
                patch("/api/alertas/$alertaId/leida")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(alertaId.toString()))
            .andExpect(jsonPath("$.leida").value(true))
            .andExpect(jsonPath("$.nombreProducto").value("Pan Lactal"))

        verify(alertaService).marcarComoLeida(alertaId, hogarId)
    }

    @Test
    @WithMockUser
    fun `Test 3 - endpoint GET por hogarId directo retorna notificaciones ordenadas`() {
        val alerta =
            AlertaResponse(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Huevos",
                diasRestantes = 4,
                leida = false,
                fechaCreacion = LocalDate.now(),
            )

        `when`(alertaService.listarPorHogar(hogarId, 0, Paginacion.TAMANO_POR_DEFECTO))
            .thenReturn(listOf(alerta))

        mockMvc
            .perform(
                get("/api/alertas/hogar/$hogarId")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombreProducto").value("Huevos"))

        verify(usuarioActualService).exigirPertenenciaAHogar(hogarId)
    }
}
