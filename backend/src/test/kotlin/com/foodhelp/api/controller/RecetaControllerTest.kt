package com.foodhelp.api.controller

import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.dto.IngredienteRecetaDTO
import com.foodhelp.api.dto.PasoRecetaDTO
import com.foodhelp.api.dto.RecetaDetalleDTO
import com.foodhelp.api.dto.RecetaExternaDTO
import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.CodigoError
import com.foodhelp.api.exception.ServicioExternoNoDisponibleException
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.InventarioService
import com.foodhelp.api.service.RecetaIntegrationService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(RecetaController::class)
@AutoConfigureMockMvc(addFilters = false)
class RecetaControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var inventarioService: InventarioService

    @MockitoBean
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @MockitoBean
    private lateinit var usuarioActualService: UsuarioActualService

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var usuarioRepository: UsuarioRepository

    private val hogarId = UUID.randomUUID()

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 1 - retorno HTTP 200 con JSON valido consultando sugerencias de recetas`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)

        val articulos =
            listOf(
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Tomate",
                    fechaVencimiento = LocalDate.now().plusDays(2),
                    cantidad = 3,
                    categoria = CategoriaEnum.VEGETALES,
                    hogarId = hogarId,
                ),
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Queso Mozzarella",
                    fechaVencimiento = LocalDate.now().plusDays(4),
                    cantidad = 1,
                    categoria = CategoriaEnum.LACTEOS,
                    hogarId = hogarId,
                ),
            )
        `when`(inventarioService.obtenerInventarioVigente(hogarId)).thenReturn(articulos)

        val recetasMock =
            listOf(
                RecetaExternaDTO(
                    id = 101,
                    title = "Pizza Margarita",
                    image = "https://img.spoonacular.com/pizza.jpg",
                    missedIngredientCount = 1,
                ),
                RecetaExternaDTO(
                    id = 102,
                    title = "Ensalada Caprese",
                    image = "https://img.spoonacular.com/caprese.jpg",
                    missedIngredientCount = 0,
                ),
            )
        `when`(recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("Tomate", "Queso Mozzarella")))
            .thenReturn(recetasMock)

        mockMvc
            .perform(
                get("/api/recetas/sugerencias")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(101))
            .andExpect(jsonPath("$[0].title").value("Pizza Margarita"))
            .andExpect(jsonPath("$[0].missedIngredientCount").value(1))
            .andExpect(jsonPath("$[1].id").value(102))
            .andExpect(jsonPath("$[1].title").value("Ensalada Caprese"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 2 - retorno HTTP 400 cuando el inventario esta vacio`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)
        `when`(inventarioService.obtenerInventarioVigente(hogarId)).thenReturn(emptyList())

        mockMvc
            .perform(
                get("/api/recetas/sugerencias")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("El inventario del hogar está vacío"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 3 - detalle de receta con ingredientes y pasos`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)
        `when`(recetaIntegrationService.obtenerDetalleReceta(101)).thenReturn(
            RecetaDetalleDTO(
                id = 101,
                title = "Pizza Margarita",
                image = "https://img.spoonacular.com/pizza.jpg",
                readyInMinutes = 25,
                servings = 4,
                ingredients =
                    listOf(
                        IngredienteRecetaDTO("tomate", 2.0, "unidad", "2 tomates"),
                    ),
                steps =
                    listOf(
                        PasoRecetaDTO(1, "Cortar el tomate"),
                        PasoRecetaDTO(2, "Hornear 10 minutos"),
                    ),
            ),
        )

        mockMvc
            .perform(get("/api/recetas/101"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(101))
            .andExpect(jsonPath("$.title").value("Pizza Margarita"))
            .andExpect(jsonPath("$.readyInMinutes").value(25))
            .andExpect(jsonPath("$.servings").value(4))
            .andExpect(jsonPath("$.ingredients[0].original").value("2 tomates"))
            .andExpect(jsonPath("$.steps[0].number").value(1))
            .andExpect(jsonPath("$.steps[1].step").value("Hornear 10 minutos"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 4 - id cero responde 400 con codigo de solicitud invalida`() {
        mockMvc
            .perform(get("/api/recetas/0"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 5 - id no numerico responde 400 con codigo de parametro invalido`() {
        mockMvc
            .perform(get("/api/recetas/no-es-un-id"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 6 - detalle sin hogar responde 403 con codigo sin hogar`() {
        `when`(usuarioActualService.hogarIdActual())
            .thenThrow(AccesoDenegadoException("El usuario autenticado no está asociado a ningún hogar", CodigoError.SIN_HOGAR))

        mockMvc
            .perform(get("/api/recetas/101"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.codigo").value("SIN_HOGAR"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 7 - receta inexistente responde 404 con su codigo`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)
        `when`(recetaIntegrationService.obtenerDetalleReceta(999)).thenReturn(null)

        mockMvc
            .perform(get("/api/recetas/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.codigo").value("RECETA_NO_ENCONTRADA"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 8 - fallo del servicio externo responde 502 con su codigo`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)
        `when`(recetaIntegrationService.obtenerDetalleReceta(101)).thenThrow(ServicioExternoNoDisponibleException())

        mockMvc
            .perform(get("/api/recetas/101"))
            .andExpect(status().isBadGateway)
            .andExpect(jsonPath("$.codigo").value("SERVICIO_EXTERNO_NO_DISPONIBLE"))
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 9 - sugerencias usan el nombre en ingles y conservan el nombre cuando falta`() {
        `when`(usuarioActualService.hogarIdActual()).thenReturn(hogarId)
        val articulos =
            listOf(
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Leche",
                    fechaVencimiento = LocalDate.now().plusDays(1),
                    cantidad = 1,
                    categoria = CategoriaEnum.LACTEOS,
                    hogarId = hogarId,
                    nombreIngles = "milk",
                ),
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Tomate",
                    fechaVencimiento = LocalDate.now().plusDays(2),
                    cantidad = 1,
                    categoria = CategoriaEnum.VEGETALES,
                    hogarId = hogarId,
                ),
            )
        `when`(inventarioService.obtenerInventarioVigente(hogarId)).thenReturn(articulos)
        `when`(recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("milk", "Tomate")))
            .thenReturn(emptyList())

        mockMvc
            .perform(get("/api/recetas/sugerencias"))
            .andExpect(status().isOk)

        verify(recetaIntegrationService).obtenerRecetasPorIngredientes(listOf("milk", "Tomate"))
    }
}
