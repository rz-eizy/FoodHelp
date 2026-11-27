package com.foodhelp.api.controller

import com.foodhelp.api.models.ArticuloDespensa
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class DetalleArticuloTest {
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
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var clock: Clock

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun limpiar() {
        alertaRepository.deleteAll()
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    private fun crearUsuarioConHogar(
        correo: String,
        conHogar: Boolean = true,
    ): Pair<Hogar?, String> {
        val hogar =
            if (conHogar) {
                hogarRepository.save(Hogar(nombre = "Hogar $correo", codigoHogar = "HG-$correo"))
            } else {
                null
            }
        val usuario =
            usuarioRepository.save(
                Usuario(
                    nombre = "Ana",
                    contrasena = checkNotNull(passwordEncoder.encode("Password123")),
                    correo = correo,
                    hogar = hogar,
                    hogarDesde = if (hogar != null) Instant.parse("2026-01-01T00:00:00Z") else null,
                ),
            )
        return hogar to "Bearer " + jwtService.tokenParaUsuario(usuario)
    }

    private fun crearArticulo(
        token: String,
        vencimiento: LocalDate,
        nombreIngles: String? = "milk",
    ): String =
        mockMvc
            .perform(
                post("/api/inventario")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "nombre" to "Leche",
                                "fechaVencimiento" to vencimiento.toString(),
                                "cantidad" to 2,
                                "nombreIngles" to nombreIngles,
                            ),
                        ),
                    ),
            ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

    @Test
    fun `devuelve el articulo creado con su nombre en ingles`() {
        val (_, token) = crearUsuarioConHogar("detalle@test.com")
        val id =
            crearArticulo(token, LocalDate.now(clock).plusDays(30))
                .let { objectMapper.readTree(it).get("id").asText() }

        mockMvc
            .perform(get("/api/inventario/$id").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.nombre").value("Leche"))
            .andExpect(jsonPath("$.nombreIngles").value("milk"))
            .andExpect(jsonPath("$.cantidad").value(2))
    }

    @Test
    fun `un articulo ajeno y uno inexistente devuelven el mismo 404`() {
        val (_, token) = crearUsuarioConHogar("propietario@test.com")
        val otroHogar = hogarRepository.save(Hogar(nombre = "Ajeno", codigoHogar = "OTRO-DETALLE"))
        val ajeno =
            articuloRepository.save(
                ArticuloDespensa(
                    nombre = "Leche ajena",
                    fechaVencimiento = LocalDate.now(clock).plusDays(5),
                    cantidad = 1,
                    hogar = otroHogar,
                ),
            )
        val inexistente = UUID.randomUUID()

        val respuestaAjena =
            mockMvc
                .perform(get("/api/inventario/${ajeno.id}").header("Authorization", token))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.codigo").value("ARTICULO_NO_ENCONTRADO"))
                .andReturn()
                .response
                .contentAsString
        val respuestaInexistente =
            mockMvc
                .perform(get("/api/inventario/$inexistente").header("Authorization", token))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.codigo").value("ARTICULO_NO_ENCONTRADO"))
                .andReturn()
                .response
                .contentAsString

        assertEquals(respuestaAjena.replace(ajeno.id.toString(), "<id>"), respuestaInexistente.replace(inexistente.toString(), "<id>"))
    }

    @Test
    fun `sin token responde 401`() {
        mockMvc
            .perform(get("/api/inventario/${UUID.randomUUID()}"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"))
    }

    @Test
    fun `sin hogar responde 403 SIN_HOGAR`() {
        val (_, token) = crearUsuarioConHogar("sinhogar@test.com", conHogar = false)

        mockMvc
            .perform(get("/api/inventario/${UUID.randomUUID()}").header("Authorization", token))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.codigo").value("SIN_HOGAR"))
    }

    @Test
    fun `un uuid invalido responde 400 PARAMETRO_INVALIDO`() {
        val (_, token) = crearUsuarioConHogar("uuid@test.com")

        mockMvc
            .perform(get("/api/inventario/no-es-un-uuid").header("Authorization", token))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
    }

    @Test
    fun `se abre el articulo desde una alerta y da 404 cuando se agota`() {
        val (_, token) = crearUsuarioConHogar("flujo@test.com")
        val id =
            crearArticulo(token, LocalDate.now(clock).plusDays(3))
                .let { objectMapper.readTree(it).get("id").asText() }
        val alertaId =
            mockMvc
                .perform(get("/api/alertas").header("Authorization", token))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].articuloId").value(id))
                .andReturn()
                .response
                .contentAsString
                .let { objectMapper.readTree(it)[0].get("id").asText() }

        mockMvc
            .perform(get("/api/inventario/$id").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))

        mockMvc
            .perform(
                patch("/api/inventario/$id/restar?cantidad=2")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.cantidad").value(0))

        mockMvc
            .perform(get("/api/inventario/$id").header("Authorization", token))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.codigo").value("ARTICULO_NO_ENCONTRADO"))
        assertNull(alertaRepository.findById(UUID.fromString(alertaId)).orElse(null))
    }
}
