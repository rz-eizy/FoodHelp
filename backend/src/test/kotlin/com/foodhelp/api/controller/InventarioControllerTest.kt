package com.foodhelp.api.controller

import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import com.foodhelp.api.service.InventarioService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(InventarioController::class)
@AutoConfigureMockMvc(addFilters = false)
class InventarioControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var inventarioService: InventarioService

    @MockitoBean
    private lateinit var usuarioRepository: UsuarioRepository

    @MockitoBean
    private lateinit var jwtService: JwtService

    @Test
    @WithMockUser
    fun `Test 1 - verificar retorno de un JSON con lista de productos al proveer ID de hogar y estado HTTP 200`() {
        val hogarId = UUID.randomUUID()
        val articulos =
            listOf(
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Leche Entera",
                    fechaVencimiento = LocalDate.now().plusDays(5),
                    cantidad = 4,
                    categoria = CategoriaEnum.LACTEOS,
                    hogarId = hogarId,
                ),
                ArticuloDTO(
                    id = UUID.randomUUID(),
                    nombre = "Pechuga de Pollo",
                    fechaVencimiento = LocalDate.now().plusDays(2),
                    cantidad = 2,
                    categoria = CategoriaEnum.CARNES,
                    hogarId = hogarId,
                ),
            )

        `when`(inventarioService.obtenerInventarioPorHogar(hogarId)).thenReturn(articulos)

        mockMvc
            .perform(
                get("/api/inventario/hogar/$hogarId")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].nombre").value("Leche Entera"))
            .andExpect(jsonPath("$[0].cantidad").value(4))
            .andExpect(jsonPath("$[1].nombre").value("Pechuga de Pollo"))
            .andExpect(jsonPath("$[1].cantidad").value(2))
    }

    @Test
    @WithMockUser
    fun `Test 2 - validar estado HTTP 400 cuando el servicio lanza excepcion por unidades negativas`() {
        val articuloId = UUID.randomUUID()
        val cantidadARestar = 10

        `when`(inventarioService.restarCantidad(articuloId, cantidadARestar))
            .thenThrow(CantidadInsuficienteException("La deducción excede el stock actual disponible"))

        mockMvc
            .perform(
                patch("/api/inventario/$articuloId/restar")
                    .param("cantidad", cantidadARestar.toString()),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    @WithMockUser(username = "chef@foodhelp.com")
    fun `Test 3 - confirmar estado HTTP 204 No Content al eliminar con exito`() {
        val articuloId = UUID.randomUUID()
        val hogarId = UUID.randomUUID()
        val email = "chef@foodhelp.com"
        val hogar = Hogar(id = hogarId, nombre = "Hogar Chef", codigoHogar = "CHEF-01")
        val usuario =
            Usuario(
                nombre = "Gordon",
                correo = email,
                contrasena = "secret123",
                hogar = hogar,
            )

        `when`(usuarioRepository.findByCorreo(email)).thenReturn(usuario)

        mockMvc
            .perform(
                delete("/api/inventario/$articuloId"),
            ).andExpect(status().isNoContent)

        verify(inventarioService).eliminarArticulo(articuloId, hogarId)
    }
}
