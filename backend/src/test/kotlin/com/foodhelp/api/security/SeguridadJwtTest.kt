package com.foodhelp.api.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadJwtTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper =
        tools.jackson.module.kotlin
            .jacksonObjectMapper()

    @org.springframework.beans.factory.annotation.Qualifier("limitadorLogin")
    @Autowired
    private lateinit var limitadorLogin: LimitadorIntentos

    @org.springframework.beans.factory.annotation.Qualifier("limitadorUnirse")
    @Autowired
    private lateinit var limitadorUnirse: LimitadorIntentos

    @org.junit.jupiter.api.BeforeEach
    fun preparar() {
        limitadorLogin.limpiarTodo()
        limitadorUnirse.limpiarTodo()
    }

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))
    }

    private fun protectedRequests(): List<() -> MockHttpServletRequestBuilder> {
        val randomUuid = UUID.randomUUID()
        return listOf(
            { get("/api/inventario") },
            { get("/api/alertas") },
            { get("/api/recetas/sugerencias") },
            { get("/api/hogares/ABC1234567") },
            {
                post("/api/hogares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"nombre": "Mi Hogar"}""")
            },
            {
                post("/api/hogares/unirse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"codigoHogar": "ABC1234567"}""")
            },
            {
                post("/api/inventario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Arroz",
                            "fechaVencimiento": "2026-12-31",
                            "cantidad": 1,
                            "categoria": "DESPENSA"
                        }
                        """.trimIndent(),
                    )
            },
            {
                patch("/api/inventario/$randomUuid/restar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"cantidad": 1}""")
            },
            { delete("/api/inventario/$randomUuid") },
            { patch("/api/alertas/$randomUuid/leida") },
        )
    }

    @Test
    fun `sin cabecera Authorization todos los endpoints protegidos responden 401`() {
        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(requestBuilder())
                .andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `con token malformado todos los endpoints protegidos responden 401`() {
        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Bearer token.invalido.xxx"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `con token expirado todos los endpoints protegidos responden 401`() {
        val token =
            Jwts
                .builder()
                .setSubject("ana@test.com")
                .setIssuedAt(Date(System.currentTimeMillis() - 200_000))
                .setExpiration(Date(System.currentTimeMillis() - 100_000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact()

        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Bearer $token"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `con token firmado con otra clave todos los endpoints protegidos responden 401`() {
        val otraClave =
            Keys.hmacShaKeyFor(
                "clave_secreta_distinta_de_al_menos_32_bytes_de_longitud"
                    .toByteArray(StandardCharsets.UTF_8),
            )
        val token =
            Jwts
                .builder()
                .setSubject("ana@test.com")
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + 3_600_000))
                .signWith(otraClave, SignatureAlgorithm.HS256)
                .compact()

        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Bearer $token"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `con token sin firma todos los endpoints protegidos responden 401`() {
        val token =
            Jwts
                .builder()
                .setSubject("ana@test.com")
                .setExpiration(Date(System.currentTimeMillis() + 3_600_000))
                .compact()

        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Bearer $token"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `con esquema de autorizacion distinto todos los endpoints protegidos responden 401`() {
        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Basic abc"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `token valido de usuario inexistente en base de datos responde 401`() {
        val token =
            Jwts
                .builder()
                .setSubject("usuario_inexistente@test.com")
                .claim("tv", 0)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + 3_600_000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact()

        for (requestBuilder in protectedRequests()) {
            mockMvc
                .perform(
                    requestBuilder().header("Authorization", "Bearer $token"),
                ).andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `token sin claim tv responde 401`() {
        val correo = "sin_tv_${UUID.randomUUID()}@test.com"
        val registro =
            """
            {
                "nombre": "Usuario Sin TV",
                "correo": "$correo",
                "contrasena": "Password123"
            }
            """.trimIndent()
        mockMvc
            .perform(
                post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registro),
            ).andExpect(status().isCreated)

        val tokenSinTv =
            Jwts
                .builder()
                .setSubject(correo)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + 3_600_000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact()

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", "Bearer $tokenSinTv"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `flujo de login, logout e invalidacion de token`() {
        val correo = "revocacion_${UUID.randomUUID()}@test.com"
        val password = "Password123"
        val registro =
            """
            {
                "nombre": "Usuario Revocacion",
                "correo": "$correo",
                "contrasena": "$password"
            }
            """.trimIndent()

        val registerRes =
            mockMvc
                .perform(
                    post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro),
                ).andExpect(status().isCreated)
                .andReturn()

        val token1 = objectMapper.readTree(registerRes.response.contentAsString).get("token").asText()

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", "Bearer $token1"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/usuarios/me/logout")
                    .header("Authorization", "Bearer $token1"),
            ).andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", "Bearer $token1"),
            ).andExpect(status().isUnauthorized)

        val loginBody =
            """
            {
                "correo": "$correo",
                "contrasena": "$password"
            }
            """.trimIndent()

        val loginRes =
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody),
                ).andExpect(status().isOk)
                .andReturn()

        val token2 = objectMapper.readTree(loginRes.response.contentAsString).get("token").asText()

        mockMvc
            .perform(
                get("/api/usuarios/me")
                    .header("Authorization", "Bearer $token2"),
            ).andExpect(status().isOk)
    }

    @Test
    fun `acceso publico login con credenciales invalidas responde 401`() {
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "correo": "noexiste@test.com",
                            "contrasena": "claveInvalida123"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `acceso publico actuator health sin token responde 200`() {
        mockMvc
            .perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }
}
