package com.foodhelp.api.controller

import com.foodhelp.api.models.Alerta
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
import java.util.Optional
import java.util.UUID

@WebMvcTest(AlertaController::class)
@AutoConfigureMockMvc(addFilters = false)
class AlertaControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var alertaRepository: AlertaRepository

    @MockitoBean
    private lateinit var usuarioRepository: UsuarioRepository

    @MockitoBean
    private lateinit var jwtService: JwtService

    private val testEmail = "usuario@foodhelp.com"
    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Alertas", codigoHogar = "ALERT-01")
    private val usuario = Usuario(nombre = "Maria", correo = testEmail, contrasena = "pass123", hogar = hogar)

    @Test
    @WithMockUser(username = "usuario@foodhelp.com")
    fun `Test 1 - recuperacion de la lista de alertas del hogar con HTTP 200`() {
        `when`(usuarioRepository.findByCorreo(testEmail)).thenReturn(usuario)

        val alerta1 =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Leche Entera",
                diasRestantes = 2,
                hogarId = hogarId,
                leida = false,
                fechaCreacion = LocalDate.now(),
            )
        val alerta2 =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Yogur de Frutilla",
                diasRestantes = 5,
                hogarId = hogarId,
                leida = true,
                fechaCreacion = LocalDate.now().minusDays(1),
            )

        `when`(alertaRepository.findByHogarIdOrderByFechaCreacionDesc(hogarId))
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
    @WithMockUser(username = "usuario@foodhelp.com")
    fun `Test 2 - actualizacion del estado de lectura con PATCH a leida true`() {
        val alertaId = UUID.randomUUID()
        val alertaOriginal =
            Alerta(
                id = alertaId,
                articuloId = UUID.randomUUID(),
                nombreProducto = "Pan Lactal",
                diasRestantes = 1,
                hogarId = hogarId,
                leida = false,
                fechaCreacion = LocalDate.now(),
            )
        val alertaModificada =
            Alerta(
                id = alertaId,
                articuloId = alertaOriginal.articuloId,
                nombreProducto = "Pan Lactal",
                diasRestantes = 1,
                hogarId = hogarId,
                leida = true,
                fechaCreacion = alertaOriginal.fechaCreacion,
            )

        `when`(alertaRepository.findById(alertaId)).thenReturn(Optional.of(alertaOriginal))
        `when`(alertaRepository.save(any(Alerta::class.java))).thenReturn(alertaModificada)

        mockMvc
            .perform(
                patch("/api/alertas/$alertaId/leida")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(alertaId.toString()))
            .andExpect(jsonPath("$.leida").value(true))
            .andExpect(jsonPath("$.nombreProducto").value("Pan Lactal"))

        verify(alertaRepository).save(any(Alerta::class.java))
    }

    @Test
    @WithMockUser(username = "usuario@foodhelp.com")
    fun `Test 3 - endpoint GET por hogarId directo retorna notificaciones ordenadas`() {
        val alerta =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Huevos",
                diasRestantes = 4,
                hogarId = hogarId,
                leida = false,
                fechaCreacion = LocalDate.now(),
            )

        `when`(alertaRepository.findByHogarIdOrderByFechaCreacionDesc(hogarId))
            .thenReturn(listOf(alerta))

        mockMvc
            .perform(
                get("/api/alertas/hogar/$hogarId")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombreProducto").value("Huevos"))
    }
}
