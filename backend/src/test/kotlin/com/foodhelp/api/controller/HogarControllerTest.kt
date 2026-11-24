package com.foodhelp.api.controller

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.tokenParaUsuario
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class HogarControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper =
        tools.jackson.module.kotlin
            .jacksonObjectMapper()

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @org.springframework.beans.factory.annotation.Qualifier("limitadorUnirse")
    @Autowired
    private lateinit var limitadorUnirse: com.foodhelp.api.security.LimitadorIntentos

    @org.junit.jupiter.api.BeforeEach
    fun preparar() {
        limitadorUnirse.limpiarTodo()
    }

    @AfterEach
    fun limpiar() {
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    @Test
    fun `Test 1 - crear hogar retorna HTTP 201 Created al instanciar un hogar nuevo`() {
        val usuario = usuarioRepository.save(Usuario(nombre = "Carlos", contrasena = "pass123", correo = "carlos@test.com", hogar = null))
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        val request = CreateHogarRequest(nombre = "Mi Casa")

        mockMvc
            .perform(
                post("/api/hogares")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.codigoHogar").isNotEmpty)
            .andExpect(jsonPath("$.nombre").value("Mi Casa"))
    }

    @Test
    fun `Test 2 - unirse con codigo de invitacion inexistente retorna HTTP 404`() {
        val usuario = usuarioRepository.save(Usuario(nombre = "Laura", contrasena = "pass123", correo = "laura@test.com", hogar = null))
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        val request = JoinHogarRequest(codigoHogar = "NOEXISTE")

        mockMvc
            .perform(
                post("/api/hogares/unirse")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `Test 3 - el DTO de respuesta omite estrictamente las contrasenas de la base de datos`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Casa Segura", codigoHogar = "SEC1234567"))
        val contrasenaSecreta = "password_secreto_123"
        val usuario =
            usuarioRepository.save(
                Usuario(nombre = "Carlos", contrasena = contrasenaSecreta, correo = "carlos2@test.com", hogar = hogar),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        val result =
            mockMvc
                .perform(
                    get("/api/hogares/SEC1234567")
                        .header("Authorization", token),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.usuarios[0].nombre").value("Carlos"))
                .andExpect(jsonPath("$.usuarios[0].contrasena").doesNotExist())
                .andReturn()

        val responseContent = result.response.contentAsString
        assertFalse(responseContent.contains("contrasena"), "El JSON de respuesta no debe contener el campo 'contrasena'")
        assertFalse(responseContent.contains(contrasenaSecreta), "El JSON de respuesta no debe exponer el valor de la contraseña")
    }

    @Test
    fun `Test 4 - usuario con hogar sale responde 204 y luego inventario responde 403`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Salir", codigoHogar = "SALIR12345"))
        val usuario =
            usuarioRepository.save(
                Usuario(nombre = "Mario", contrasena = "pass123", correo = "mario@test.com", hogar = hogar),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(post("/api/hogares/salir").header("Authorization", token))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/inventario").header("Authorization", token))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `Test 5 - usuario sin hogar al llamar salir responde 409`() {
        val usuario =
            usuarioRepository.save(
                Usuario(nombre = "SinHogar", contrasena = "pass123", correo = "sinhogar@test.com", hogar = null),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(post("/api/hogares/salir").header("Authorization", token))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("El usuario no pertenece a ningún hogar"))
    }

    @Test
    fun `Test 6 - si el usuario era el unico miembro el hogar y sus articulos se eliminan de la base de datos`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Solo", codigoHogar = "SOLO123456"))
        val usuario =
            usuarioRepository.save(
                Usuario(nombre = "Solo", contrasena = "pass123", correo = "solo@test.com", hogar = hogar),
            )
        val articulo =
            articuloRepository.save(
                ArticuloDespensa(
                    nombre = "Manzanas",
                    fechaVencimiento = LocalDate.now().plusDays(5),
                    cantidad = 4,
                    categoria = CategoriaEnum.FRUTAS,
                    hogar = hogar,
                ),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(post("/api/hogares/salir").header("Authorization", token))
            .andExpect(status().isNoContent)

        assertFalse(hogarRepository.existsById(hogar.id!!))
        assertFalse(articuloRepository.existsById(articulo.id!!))
    }

    @Test
    fun `Test 7 - si habia otro miembro el hogar sigue existiendo y el otro usuario conserva su acceso`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Compartido", codigoHogar = "COMP123456"))
        val user1 =
            usuarioRepository.save(
                Usuario(nombre = "User1", contrasena = "pass123", correo = "user1@test.com", hogar = hogar),
            )
        val user2 =
            usuarioRepository.save(
                Usuario(nombre = "User2", contrasena = "pass123", correo = "user2@test.com", hogar = hogar),
            )
        val token1 = "Bearer " + jwtService.tokenParaUsuario(user1)
        val token2 = "Bearer " + jwtService.tokenParaUsuario(user2)

        mockMvc
            .perform(post("/api/hogares/salir").header("Authorization", token1))
            .andExpect(status().isNoContent)

        assertTrue(hogarRepository.existsById(hogar.id!!))

        mockMvc
            .perform(get("/api/inventario").header("Authorization", token2))
            .andExpect(status().isOk)
    }

    @Test
    fun `el creador del hogar es propietario y registra hogarDesde`() {
        val usuario = usuarioRepository.save(Usuario(nombre = "Carlos", contrasena = "pass123", correo = "carlos@test.com", hogar = null))
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(
                post("/api/hogares")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(CreateHogarRequest(nombre = "Mi Casa"))),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.esPropietario").value(true))
            .andExpect(jsonPath("$.usuarios[0].esPropietario").value(true))

        val actualizado = usuarioRepository.findById(usuario.id!!).get()
        assertTrue(actualizado.hogarDesde != null)
    }

    @Test
    fun `unirse a un hogar sin propietario convierte al nuevo integrante en propietario`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Sin Propietario", codigoHogar = "SINPROPI1"))
        val primero = usuarioRepository.save(Usuario(nombre = "Ana", contrasena = "pass123", correo = "ana@test.com", hogar = hogar))
        val segundo = usuarioRepository.save(Usuario(nombre = "Luis", contrasena = "pass123", correo = "luis@test.com", hogar = null))
        val tokenSegundo = "Bearer " + jwtService.tokenParaUsuario(segundo)

        mockMvc
            .perform(
                post("/api/hogares/unirse")
                    .header("Authorization", tokenSegundo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(JoinHogarRequest(codigoHogar = "SINPROPI1"))),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.esPropietario").value(true))

        val actualizado = hogarRepository.findById(hogar.id!!).get()
        assertTrue(actualizado.propietarioId == segundo.id)
        assertTrue(usuarioRepository.findById(segundo.id!!).get().hogarDesde != null)
    }

    @Test
    fun `perfil del usuario incluye esPropietario`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Hogar Perfil", codigoHogar = "PERFIL1234", propietarioId = null))
        val usuario = usuarioRepository.save(Usuario(nombre = "Ana", contrasena = "pass123", correo = "ana@test.com", hogar = hogar))
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(get("/api/usuarios/me").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hogar.esPropietario").value(false))

        hogar.propietarioId = usuario.id
        hogarRepository.saveAndFlush(hogar)

        mockMvc
            .perform(get("/api/usuarios/me").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hogar.esPropietario").value(true))
    }
}
