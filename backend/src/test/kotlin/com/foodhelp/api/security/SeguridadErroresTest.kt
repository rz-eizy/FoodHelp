package com.foodhelp.api.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadErroresTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val restAccessDeniedHandler: RestAccessDeniedHandler,
) {
    @Test
    fun `sin token responde 401 con codigo NO_AUTENTICADO`() {
        mockMvc
            .perform(get("/api/alertas"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("No autenticado"))
            .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"))
            .andExpect(jsonPath("$.campos").doesNotExist())
    }

    @Test
    fun `token invalido responde 401 con cuerpo json`() {
        mockMvc
            .perform(get("/api/alertas").header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"))
    }

    @Test
    fun `manejador de acceso denegado escribe 403 con codigo`() {
        val respuesta = MockHttpServletResponse()
        restAccessDeniedHandler.handle(
            MockHttpServletRequest(),
            respuesta,
            AccessDeniedException("denegado"),
        )
        assertThat(respuesta.status).isEqualTo(403)
        assertThat(respuesta.contentType).isEqualTo("application/json;charset=UTF-8")
        assertThat(respuesta.contentAsString).contains("\"codigo\":\"ACCESO_DENEGADO\"")
    }
}
