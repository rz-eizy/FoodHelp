package com.foodhelp.api.controller

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class HogarControllerTest {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val objectMapper =
        com.fasterxml.jackson.module.kotlin
            .jacksonObjectMapper()

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @AfterEach
    fun limpiar() {
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    @Test
    fun `Test 1 - crear hogar retorna HTTP 201 Created al instanciar un hogar nuevo`() {
        val request = CreateHogarRequest(nombre = "Mi Casa")

        mockMvc
            .perform(
                post("/api/hogares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.codigoHogar").isNotEmpty)
            .andExpect(jsonPath("$.nombre").value("Mi Casa"))
    }

    @Test
    fun `Test 2 - unirse con codigo de invitacion inexistente retorna HTTP 404`() {
        val request = JoinHogarRequest(codigoHogar = "NOEXISTE")

        mockMvc
            .perform(
                post("/api/hogares/unirse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `Test 3 - el DTO de respuesta omite estrictamente las contrasenas de la base de datos`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Casa Segura", codigoHogar = "SEC123"))
        val contrasenaSecreta = "password_secreto_123"
        usuarioRepository.save(Usuario(nombre = "Carlos", contrasena = contrasenaSecreta, hogar = hogar))

        val result =
            mockMvc
                .perform(get("/api/hogares/SEC123"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.usuarios[0].nombre").value("Carlos"))
                .andExpect(jsonPath("$.usuarios[0].contrasena").doesNotExist())
                .andReturn()

        val responseContent = result.response.contentAsString
        assertFalse(responseContent.contains("contrasena"), "El JSON de respuesta no debe contener el campo 'contrasena'")
        assertFalse(responseContent.contains(contrasenaSecreta), "El JSON de respuesta no debe exponer el valor de la contraseña")
    }
}
