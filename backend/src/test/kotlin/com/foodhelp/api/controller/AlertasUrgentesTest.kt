package com.foodhelp.api.controller

import com.foodhelp.api.models.Alerta
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.tokenParaUsuario
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AlertasUrgentesTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var alertaRepository: AlertaRepository

    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtService: JwtService

    @AfterEach
    fun limpiar() {
        alertaRepository.deleteAll()
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    private fun crearUsuarioConHogar(correo: String): Pair<Hogar, String> {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar $correo", codigoHogar = "CO-$correo"))
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
        return hogar to "Bearer " + jwtService.tokenParaUsuario(usuario)
    }

    private fun crearAlerta(
        hogarId: UUID,
        dias: Int,
        fechaCreacion: LocalDate = LocalDate.of(2026, 11, 1),
        leida: Boolean = false,
    ): Alerta =
        alertaRepository.save(
            Alerta(
                articuloId = UUID.randomUUID(),
                nombreProducto = "Producto $dias",
                diasRestantes = dias,
                hogarId = hogarId,
                leida = leida,
                fechaCreacion = fechaCreacion,
            ),
        )

    @Test
    fun `lista las alertas por dias restantes ascendente aunque se inserten mezcladas`() {
        val (hogar, token) = crearUsuarioConHogar("orden@test.com")
        crearAlerta(hogar.id!!, 7)
        crearAlerta(hogar.id!!, 0)
        crearAlerta(hogar.id!!, 3)
        crearAlerta(hogar.id!!, -2)

        mockMvc
            .perform(get("/api/alertas?size=10").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].diasRestantes").value(-2))
            .andExpect(jsonPath("$[1].diasRestantes").value(0))
            .andExpect(jsonPath("$[2].diasRestantes").value(3))
            .andExpect(jsonPath("$[3].diasRestantes").value(7))
    }

    @Test
    fun `el desempate usa fecha de creacion ascendente y luego id`() {
        val (hogar, token) = crearUsuarioConHogar("empate@test.com")
        crearAlerta(hogar.id!!, 2, LocalDate.of(2026, 11, 3))
        crearAlerta(hogar.id!!, 2, LocalDate.of(2026, 11, 1))
        crearAlerta(hogar.id!!, 2, LocalDate.of(2026, 11, 2))
        val mismoDia = listOf(crearAlerta(hogar.id!!, 4), crearAlerta(hogar.id!!, 4))
        val idsEsperados = mismoDia.map { checkNotNull(it.id).toString() }.sorted()

        mockMvc
            .perform(get("/api/alertas?size=10").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].fechaCreacion").value("2026-11-01"))
            .andExpect(jsonPath("$[1].fechaCreacion").value("2026-11-02"))
            .andExpect(jsonPath("$[2].fechaCreacion").value("2026-11-03"))
            .andExpect(jsonPath("$[3].id").value(idsEsperados[0]))
            .andExpect(jsonPath("$[4].id").value(idsEsperados[1]))
    }

    @Test
    fun `el filtro por leida conserva el orden por urgencia`() {
        val (hogar, token) = crearUsuarioConHogar("filtro@test.com")
        crearAlerta(hogar.id!!, 5, leida = true)
        crearAlerta(hogar.id!!, -1)
        crearAlerta(hogar.id!!, 2, leida = true)
        crearAlerta(hogar.id!!, 1)

        mockMvc
            .perform(get("/api/alertas?leida=false&size=10").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].diasRestantes").value(-1))
            .andExpect(jsonPath("$[1].diasRestantes").value(1))

        mockMvc
            .perform(get("/api/alertas?leida=true&size=10").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].diasRestantes").value(2))
            .andExpect(jsonPath("$[1].diasRestantes").value(5))
    }

    @Test
    fun `la paginacion es contigua y no repite filas`() {
        val (hogar, token) = crearUsuarioConHogar("paginas@test.com")
        val alertas =
            listOf(crearAlerta(hogar.id!!, 9), crearAlerta(hogar.id!!, 4), crearAlerta(hogar.id!!, -3), crearAlerta(hogar.id!!, 6))
        val ordenEsperado = alertas.sortedBy { it.diasRestantes }.map { checkNotNull(it.id).toString() }

        val primera =
            mockMvc
                .perform(get("/api/alertas?page=0&size=2").header("Authorization", token))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].diasRestantes").value(-3))
                .andExpect(jsonPath("$[1].diasRestantes").value(4))
        val segunda =
            mockMvc
                .perform(get("/api/alertas?page=1&size=2").header("Authorization", token))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].diasRestantes").value(6))
                .andExpect(jsonPath("$[1].diasRestantes").value(9))

        assertEquals(
            ordenEsperado,
            listOf(
                primera.andReturn().response.contentAsString,
                segunda.andReturn().response.contentAsString,
            ).flatMap { json ->
                Regex("\"id\":\"([0-9a-f-]{36})\"").findAll(json).map { it.groupValues[1] }.toList()
            },
        )
    }

    @Test
    fun `el listado por hogar usa el mismo orden y no incluye alertas ajenas`() {
        val (hogar, token) = crearUsuarioConHogar("propio@test.com")
        crearAlerta(hogar.id!!, 6)
        crearAlerta(hogar.id!!, -2)
        val otroHogar = hogarRepository.save(Hogar(nombre = "Ajeno", codigoHogar = "OTRO-HOGAR"))
        crearAlerta(otroHogar.id!!, -100)

        mockMvc
            .perform(get("/api/alertas/hogar/${hogar.id}?size=10").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].diasRestantes").value(-2))
            .andExpect(jsonPath("$[1].diasRestantes").value(6))
    }
}
