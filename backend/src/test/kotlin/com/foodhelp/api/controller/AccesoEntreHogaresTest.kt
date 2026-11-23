package com.foodhelp.api.controller

import com.foodhelp.api.models.Alerta
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AccesoEntreHogaresTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var alertaRepository: AlertaRepository

    @Autowired
    private lateinit var jwtService: JwtService

    private lateinit var hogarPropio: Hogar
    private lateinit var hogarAjeno: Hogar
    private lateinit var bearerAna: String

    @BeforeEach
    fun preparar() {
        hogarPropio = hogarRepository.save(Hogar(nombre = "Propio", codigoHogar = "PROPIO0001"))
        hogarAjeno = hogarRepository.save(Hogar(nombre = "Ajeno", codigoHogar = "AJENO00001"))
        val ana =
            usuarioRepository.save(
                Usuario(nombre = "Ana", contrasena = "no-importa", correo = "ana@test.com", hogar = hogarPropio),
            )
        bearerAna = "Bearer " + jwtService.tokenParaUsuario(ana)
    }

    @AfterEach
    fun limpiar() {
        alertaRepository.deleteAll()
        articuloRepository.deleteAll()
        usuarioRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    private fun articulo(
        hogar: Hogar,
        cantidad: Int,
    ) = articuloRepository.save(
        ArticuloDespensa(
            nombre = "Arroz",
            fechaVencimiento = LocalDate.now().plusDays(30),
            cantidad = cantidad,
            hogar = hogar,
        ),
    )

    @Test
    fun `sin token se responde 401`() {
        mockMvc.perform(get("/api/inventario")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `no se puede leer el inventario de otro hogar`() {
        mockMvc
            .perform(get("/api/inventario/hogar/${hogarAjeno.id}").header("Authorization", bearerAna))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `no se puede restar stock de un articulo de otro hogar`() {
        val ajeno = articulo(hogarAjeno, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${ajeno.id}/restar")
                    .param("cantidad", "1")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)

        assertEquals(5, articuloRepository.findById(ajeno.id!!).get().cantidad)
    }

    @Test
    fun `no se puede eliminar un articulo de otro hogar`() {
        val ajeno = articulo(hogarAjeno, 5)

        mockMvc
            .perform(
                delete("/api/inventario/${ajeno.id}")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)

        assertTrue(articuloRepository.existsById(ajeno.id!!))
    }

    @Test
    fun `no se puede marcar como leida una alerta de otro hogar`() {
        val ajeno = articulo(hogarAjeno, 2)
        val alertaAjena =
            alertaRepository.save(
                Alerta(
                    articuloId = ajeno.id!!,
                    nombreProducto = "Arroz",
                    diasRestantes = 3,
                    hogarId = hogarAjeno.id!!,
                    leida = false,
                    fechaCreacion = LocalDate.now(),
                ),
            )

        mockMvc
            .perform(
                patch("/api/alertas/${alertaAjena.id}/leida")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)

        assertFalse(alertaRepository.findById(alertaAjena.id!!).get().leida)
    }

    @Test
    fun `un UUID inexistente devuelve exactamente el mismo codigo y estructura de cuerpo que un UUID ajeno`() {
        val ajeno = articulo(hogarAjeno, 5)
        val uuidInexistente = UUID.randomUUID()

        mockMvc
            .perform(
                patch("/api/inventario/${ajeno.id}/restar")
                    .param("cantidad", "1")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())

        mockMvc
            .perform(
                patch("/api/inventario/$uuidInexistente/restar")
                    .param("cantidad", "1")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `una cantidad negativa se rechaza con 400 y no altera el stock`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}/restar")
                    .param("cantidad", "-3")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)

        assertEquals(5, articuloRepository.findById(propio.id!!).get().cantidad)
    }

    @Test
    fun `restar con body cantidad negativa responde 400 con campo y no altera stock`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}/restar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"cantidad": -5}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.campos.cantidad").exists())

        assertEquals(5, articuloRepository.findById(propio.id!!).get().cantidad)
    }

    @Test
    fun `restar con body cantidad cero responde 400 y no altera stock`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}/restar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"cantidad": 0}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)

        assertEquals(5, articuloRepository.findById(propio.id!!).get().cantidad)
    }

    @Test
    fun `crear articulo sin fechaVencimiento responde 400 y no altera el inventario`() {
        val totalAntes = articuloRepository.count()

        mockMvc
            .perform(
                post("/api/inventario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Leche",
                            "cantidad": 2,
                            "categoria": "LACTEOS"
                        }
                        """.trimIndent(),
                    ).header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())

        assertEquals(totalAntes, articuloRepository.count())
    }

    @Test
    fun `obtenerHogar con codigo ajeno y con codigo inexistente responden exactamente 404 y misma estructura`() {
        mockMvc
            .perform(
                get("/api/hogares/${hogarAjeno.codigoHogar}")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())

        mockMvc
            .perform(
                get("/api/hogares/NOEXISTE99")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `actualizar nombre cantidad y categoria de un articulo propio responde 200 con valores nuevos`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Arroz Integral",
                            "cantidad": 8,
                            "categoria": "DESPENSA"
                        }
                        """.trimIndent(),
                    ).header("Authorization", bearerAna),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Arroz Integral"))
            .andExpect(jsonPath("$.cantidad").value(8))
            .andExpect(jsonPath("$.categoria").value("DESPENSA"))

        val actualizado = articuloRepository.findById(propio.id!!).get()
        assertEquals("Arroz Integral", actualizado.nombre)
        assertEquals(8, actualizado.cantidad)
    }

    @Test
    fun `actualizar con body vacio responde 400`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Debes enviar al menos un campo para actualizar"))
    }

    @Test
    fun `actualizar con cantidad negativa responde 400 con campos cantidad`() {
        val propio = articulo(hogarPropio, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"cantidad": -1}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.campos.cantidad").exists())
    }

    @Test
    fun `actualizar articulo de otro hogar responde 404 sin cambios`() {
        val ajeno = articulo(hogarAjeno, 5)

        mockMvc
            .perform(
                patch("/api/inventario/${ajeno.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"nombre": "Hack"}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isNotFound)

        val noCambiado = articuloRepository.findById(ajeno.id!!).get()
        assertEquals("Arroz", noCambiado.nombre)
    }

    @Test
    fun `cambiar fechaVencimiento elimina la alerta existente de ese articulo`() {
        val propio = articulo(hogarPropio, 5)
        val alerta =
            alertaRepository.save(
                Alerta(
                    articuloId = propio.id!!,
                    nombreProducto = "Arroz",
                    diasRestantes = 2,
                    hogarId = hogarPropio.id!!,
                    leida = false,
                    fechaCreacion = LocalDate.now(),
                ),
            )
        assertTrue(alertaRepository.existsById(alerta.id!!))

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fechaVencimiento": "2026-12-31"}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isOk)

        assertFalse(alertaRepository.existsById(alerta.id!!))
    }

    @Test
    fun `crear articulo con fecha de ayer responde 400 con error y no altera el inventario`() {
        val totalAntes = articuloRepository.count()

        mockMvc
            .perform(
                post("/api/inventario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Leche",
                            "fechaVencimiento": "${LocalDate.now().minusDays(1)}",
                            "cantidad": 2,
                            "categoria": "LACTEOS"
                        }
                        """.trimIndent(),
                    ).header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("La fecha de vencimiento no puede ser anterior a hoy"))

        assertEquals(totalAntes, articuloRepository.count())
    }

    @Test
    fun `crear articulo con fecha de hoy responde 201`() {
        mockMvc
            .perform(
                post("/api/inventario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "nombre": "Leche",
                            "fechaVencimiento": "${LocalDate.now()}",
                            "cantidad": 2,
                            "categoria": "LACTEOS"
                        }
                        """.trimIndent(),
                    ).header("Authorization", bearerAna),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.fechaVencimiento").value(LocalDate.now().toString()))
    }

    @Test
    fun `editar fecha de vencimiento a una fecha pasada responde 400 y no cambia el articulo`() {
        val propio = articulo(hogarPropio, 5)
        val fechaOriginal = propio.fechaVencimiento

        mockMvc
            .perform(
                patch("/api/inventario/${propio.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fechaVencimiento": "${LocalDate.now().minusDays(1)}"}""")
                    .header("Authorization", bearerAna),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("La fecha de vencimiento no puede ser anterior a hoy"))

        assertEquals(fechaOriginal, articuloRepository.findById(propio.id!!).get().fechaVencimiento)
    }
}
