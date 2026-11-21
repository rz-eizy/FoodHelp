package com.foodhelp.api.controller

import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.dto.RecetaExternaDTO
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.security.UsuarioActualService
import com.foodhelp.api.service.InventarioService
import com.foodhelp.api.service.RecetaIntegrationService
import org.junit.jupiter.api.Test
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
}
