package com.foodhelp.api.controller

import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.HogarRepository
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.tokenParaUsuario
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @AfterEach
    fun limpiar() {
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    @Test
    fun `sin token responde 401`() {
        mockMvc
            .perform(get("/api/usuarios/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `usuario sin hogar responde 200 con hogar nulo y sin contrasena`() {
        val usuario =
            usuarioRepository.save(
                Usuario(
                    nombre = "Pedro",
                    contrasena = "secreto123",
                    correo = "pedro@test.com",
                    hogar = null,
                ),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(usuario.id.toString()))
            .andExpect(jsonPath("$.nombre").value("Pedro"))
            .andExpect(jsonPath("$.correo").value("pedro@test.com"))
            .andExpect(jsonPath("$.hogar").value(nullValue()))
            .andExpect(jsonPath("$.contrasena").doesNotExist())
    }

    @Test
    fun `usuario con hogar responde 200 con id nombre y codigoHogar del hogar`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Casa Pedro", codigoHogar = "PEDRO12345"))
        val usuario =
            usuarioRepository.save(
                Usuario(
                    nombre = "Pedro Con Hogar",
                    contrasena = "secreto123",
                    correo = "pedro_hogar@test.com",
                    hogar = hogar,
                ),
            )
        val token = "Bearer " + jwtService.tokenParaUsuario(usuario)

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(usuario.id.toString()))
            .andExpect(jsonPath("$.nombre").value("Pedro Con Hogar"))
            .andExpect(jsonPath("$.correo").value("pedro_hogar@test.com"))
            .andExpect(jsonPath("$.hogar.id").value(hogar.id.toString()))
            .andExpect(jsonPath("$.hogar.nombre").value("Casa Pedro"))
            .andExpect(jsonPath("$.hogar.codigoHogar").value("PEDRO12345"))
            .andExpect(jsonPath("$.contrasena").doesNotExist())
    }
}
