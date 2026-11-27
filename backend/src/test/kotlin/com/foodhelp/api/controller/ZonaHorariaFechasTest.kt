package com.foodhelp.api.controller

import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarExpulsadoRepository
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.tokenParaUsuario
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@SpringBootTest
@AutoConfigureMockMvc
class ZonaHorariaFechasTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var alertaRepository: AlertaRepository

    @Autowired
    private lateinit var hogarExpulsadoRepository: HogarExpulsadoRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var clock: Clock

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun limpiar() {
        alertaRepository.deleteAll()
        hogarExpulsadoRepository.deleteAll()
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    private fun fijarHora(instant: Instant) {
        val fijo = Clock.fixed(instant, ZoneId.of("America/Santiago"))
        `when`(clock.instant()).thenReturn(fijo.instant())
        `when`(clock.millis()).thenReturn(fijo.millis())
        `when`(clock.getZone()).thenReturn(fijo.zone)
    }

    private fun tokenConHogar(correo: String): String {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Zona", codigoHogar = "HZ-$correo"))
        val usuario =
            usuarioRepository.save(
                Usuario(
                    nombre = "Ana",
                    contrasena = checkNotNull(passwordEncoder.encode("Password123")),
                    correo = correo,
                    hogar = hogar,
                    hogarDesde = Instant.parse("2026-01-01T00:00:00Z"),
                ),
            )
        return "Bearer " + jwtService.tokenParaUsuario(usuario)
    }

    private fun crearArticulo(
        token: String,
        fechaVencimiento: String,
    ) = mockMvc.perform(
        post("/api/inventario")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(
                    mapOf(
                        "nombre" to "Leche",
                        "fechaVencimiento" to fechaVencimiento,
                        "cantidad" to 1,
                    ),
                ),
            ),
    )

    @Test
    fun `vencimiento del dia local 25 se acepta a las 2230 del 25 en santiago`() {
        fijarHora(Instant.parse("2026-11-26T01:30:00Z"))
        val token = tokenConHogar("invierno@test.com")

        crearArticulo(token, "2026-11-25").andExpect(status().isCreated)
    }

    @Test
    fun `vencimiento del dia local 24 se rechaza a las 2230 del 25 en santiago`() {
        fijarHora(Instant.parse("2026-11-26T01:30:00Z"))
        val token = tokenConHogar("invierno-pasado@test.com")

        crearArticulo(token, "2026-11-24")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("FECHA_VENCIMIENTO_PASADA"))
    }

    @Test
    fun `vencimiento del dia local 30 se acepta a las 2230 del 30 en santiago en verano`() {
        fijarHora(Instant.parse("2026-07-01T02:30:00Z"))
        val token = tokenConHogar("verano@test.com")

        crearArticulo(token, "2026-06-30").andExpect(status().isCreated)
    }
}
