package com.foodhelp.api.controller

import com.foodhelp.api.security.UsuarioActualService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ErroresApiTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var usuarioActualService: UsuarioActualService

    @Test
    fun `uuid invalido en la ruta responde PARAMETRO_INVALIDO`() {
        mockMvc
            .perform(patch("/api/alertas/no-es-un-uuid/leida"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Parámetro inválido"))
            .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
            .andExpect(jsonPath("$.campos").doesNotExist())
    }

    @Test
    fun `parametro de consulta invalido responde PARAMETRO_INVALIDO`() {
        mockMvc
            .perform(get("/api/alertas").param("page", "abc"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
    }

    @Test
    fun `ruta inexistente responde RUTA_NO_ENCONTRADA`() {
        mockMvc
            .perform(get("/api/ruta-que-no-existe"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Ruta no encontrada"))
            .andExpect(jsonPath("$.codigo").value("RUTA_NO_ENCONTRADA"))
    }

    @Test
    fun `metodo no permitido responde METODO_NO_PERMITIDO`() {
        mockMvc
            .perform(delete("/api/auth/login"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"))
    }

    @Test
    fun `content type no soportado responde TIPO_CONTENIDO_NO_SOPORTADO`() {
        mockMvc
            .perform(post("/api/auth/login").contentType("text/plain").content("{}"))
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.codigo").value("TIPO_CONTENIDO_NO_SOPORTADO"))
    }

    @Test
    fun `cuerpo json malformado responde JSON_MALFORMADO`() {
        mockMvc
            .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("JSON_MALFORMADO"))
    }

    @Test
    fun `validacion de campos responde VALIDACION con campos`() {
        val cuerpo = """{"nombre":"","correo":"","contrasena":""}"""
        val peticion = post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo)
        mockMvc
            .perform(peticion)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Datos de entrada inválidos"))
            .andExpect(jsonPath("$.codigo").value("VALIDACION"))
            .andExpect(jsonPath("$.campos").exists())
    }

    @Test
    fun `error no controlado responde ERROR_INTERNO sin filtrar el detalle`() {
        Mockito.`when`(usuarioActualService.hogarIdActual()).thenThrow(RuntimeException("secreto-interno"))
        mockMvc
            .perform(get("/api/alertas"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.error").value("Error interno del servidor"))
            .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
    }
}
