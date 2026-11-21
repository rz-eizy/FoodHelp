package com.foodhelp.api.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Qualifier("limitadorLogin")
    @Autowired
    private lateinit var limitadorLogin: LimitadorIntentos

    @BeforeEach
    fun setUp() {
        limitadorLogin.limpiarTodo()
    }

    @Test
    fun `10 intentos fallidos de login bloquean el intento 11 con HTTP 429`() {
        val correo = "bloqueo_${UUID.randomUUID()}@test.com"
        val password = "Password123"

        mockMvc
            .perform(
                post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Usuario Bloqueo",
                            "correo": "$correo",
                            "contrasena": "$password"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)

        val loginErroneo =
            """
            {
                "correo": "$correo",
                "contrasena": "ClaveErronea123"
            }
            """.trimIndent()

        repeat(10) {
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginErroneo),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginErroneo),
            ).andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.error").value("Demasiados intentos. Inténtalo de nuevo más tarde"))
    }

    @Test
    fun `otro usuario con correo distinto no resulta afectado por los fallos del primero`() {
        val correoBloqueado = "bloqueado_${UUID.randomUUID()}@test.com"
        val correoLimpio = "limpio_${UUID.randomUUID()}@test.com"
        val password = "Password123"

        for (c in listOf(correoBloqueado, correoLimpio)) {
            mockMvc
                .perform(
                    post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                                "nombre": "Usuario Test",
                                "correo": "$c",
                                "contrasena": "$password"
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isCreated)
        }

        val loginErroneo =
            """
            {
                "correo": "$correoBloqueado",
                "contrasena": "ClaveErronea123"
            }
            """.trimIndent()

        repeat(10) {
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginErroneo),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginErroneo),
            ).andExpect(status().isTooManyRequests)

        val loginCorrectoLimpio =
            """
            {
                "correo": "$correoLimpio",
                "contrasena": "$password"
            }
            """.trimIndent()

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginCorrectoLimpio),
            ).andExpect(status().isOk)
    }

    @Test
    fun `un login exitoso resetea el contador de intentos fallidos`() {
        val correo = "reset_${UUID.randomUUID()}@test.com"
        val password = "Password123"

        mockMvc
            .perform(
                post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Usuario Reset",
                            "correo": "$correo",
                            "contrasena": "$password"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)

        val loginErroneo =
            """
            {
                "correo": "$correo",
                "contrasena": "ClaveErronea123"
            }
            """.trimIndent()

        val loginCorrecto =
            """
            {
                "correo": "$correo",
                "contrasena": "$password"
            }
            """.trimIndent()

        repeat(9) {
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginErroneo),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginCorrecto),
            ).andExpect(status().isOk)

        repeat(5) {
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginErroneo),
                ).andExpect(status().isUnauthorized)
        }
    }
}
