package com.foodhelp.api.controller

import com.foodhelp.api.dto.EliminarCuentaRequest
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarExpulsadoRepository
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.LimitadorIntentos
import com.foodhelp.api.security.tokenParaUsuario
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class EliminarCuentaTest {
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

    @Qualifier("limitadorLogin")
    @Autowired
    private lateinit var limitadorLogin: LimitadorIntentos

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun preparar() {
        limitadorLogin.limpiarTodo()
    }

    @AfterEach
    fun limpiar() {
        alertaRepository.deleteAll()
        hogarExpulsadoRepository.deleteAll()
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    private fun crearUsuario(
        correo: String,
        nombre: String = "Ana",
        hogar: Hogar? = null,
        hogarDesde: Instant? = null,
    ): Usuario =
        usuarioRepository.save(
            Usuario(
                nombre = nombre,
                contrasena = checkNotNull(passwordEncoder.encode("Password123")),
                correo = correo,
                hogar = hogar,
                hogarDesde = hogarDesde,
            ),
        )

    private fun eliminar(
        token: String,
        contrasena: String = "Password123",
    ) = mockMvc.perform(
        post("/api/usuarios/me/eliminar")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(EliminarCuentaRequest(contrasena))),
    )

    @Test
    fun `sin token responde 401`() {
        mockMvc
            .perform(
                post("/api/usuarios/me/eliminar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"contrasena":"Password123"}"""),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `contrasena incorrecta responde 403 y deja la cuenta activa`() {
        val usuario = crearUsuario("ana@test.com")
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token, "ClaveErronea123")
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("Contraseña incorrecta"))

        mockMvc
            .perform(get("/api/usuarios/me").header("Authorization", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.correo").value("ana@test.com"))
        val guardado = usuarioRepository.findByCorreo("ana@test.com")
        assertNotNull(guardado)
        assertNull(guardado!!.eliminadoEn)
    }

    @Test
    fun `contrasena correcta responde 204 y anonimiza los datos`() {
        val usuario = crearUsuario("ana@test.com", "Ana")
        val id = checkNotNull(usuario.id)
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token).andExpect(status().isNoContent)

        val anonimo = usuarioRepository.findById(id).orElseThrow()
        assertEquals("Usuario eliminado", anonimo.nombre)
        assertEquals("eliminado-$id@cuenta-eliminada.invalid", anonimo.correo)
        assertNotNull(anonimo.eliminadoEn)
        assertNull(anonimo.hogar)
        assertNull(anonimo.hogarDesde)
        assertFalse(passwordEncoder.matches("Password123", anonimo.contrasena))
    }

    @Test
    fun `el token anterior deja de ser valido`() {
        val usuario = crearUsuario("ana@test.com")
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token).andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/usuarios/me").header("Authorization", token))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `login con el correo original responde 401`() {
        val usuario = crearUsuario("ana@test.com")
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token).andExpect(status().isNoContent)

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"correo":"ana@test.com","contrasena":"Password123"}"""),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `el correo original se puede volver a registrar`() {
        val usuario = crearUsuario("ana@test.com")
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token).andExpect(status().isNoContent)

        mockMvc
            .perform(
                post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"nombre":"Nueva Ana","correo":"ana@test.com","contrasena":"Password123"}""",
                    ),
            ).andExpect(status().isCreated)
    }

    @Test
    fun `si era el unico integrante se eliminan hogar e inventario`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Casa Ana", codigoHogar = "CASA12345"))
        val usuario = crearUsuario("ana@test.com", hogar = hogar, hogarDesde = Instant.now())
        hogar.propietarioId = usuario.id
        hogarRepository.saveAndFlush(hogar)
        articuloRepository.save(
            ArticuloDespensa(
                nombre = "Leche",
                fechaVencimiento = LocalDate.now().plusDays(2),
                cantidad = 2,
                hogar = hogar,
            ),
        )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        eliminar(token).andExpect(status().isNoContent)

        assertFalse(hogarRepository.existsById(checkNotNull(hogar.id)))
        assertEquals(0, articuloRepository.count())
        assertNull(usuarioRepository.findByCorreo("ana@test.com"))
    }

    @Test
    fun `con mas integrantes el hogar sigue y la propiedad pasa al mas antiguo`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Casa Ana", codigoHogar = "CASA12345"))
        val antiguo = crearUsuario("ana@test.com", "Ana", hogar, Instant.now().minusSeconds(3600))
        val otro = crearUsuario("beto@test.com", "Beto", hogar, Instant.now().minusSeconds(1800))
        hogar.propietarioId = antiguo.id
        hogarRepository.saveAndFlush(hogar)
        val token = "Bearer " + jwtService.tokenParaUsuario(antiguo)

        eliminar(token).andExpect(status().isNoContent)

        val actualizado = hogarRepository.findById(checkNotNull(hogar.id)).orElseThrow()
        assertEquals(otro.id, actualizado.propietarioId)
        val tokenOtro = "Bearer " + jwtService.tokenParaUsuario(otro)
        mockMvc
            .perform(get("/api/hogares/CASA12345").header("Authorization", tokenOtro))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usuarios.length()").value(1))
            .andExpect(jsonPath("$.esPropietario").value(true))
    }

    @Test
    fun `diez contrasenas incorrectas bloquean con 429`() {
        val usuario = crearUsuario("ana@test.com")
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        repeat(10) {
            eliminar(token, "ClaveErronea123").andExpect(status().isForbidden)
        }

        eliminar(token, "ClaveErronea123").andExpect(status().isTooManyRequests)
    }
}
