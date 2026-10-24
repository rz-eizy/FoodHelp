package com.foodhelp.api.service

import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.ArticuloRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InventarioServiceTest {
    @Mock
    private lateinit var articuloRepository: ArticuloRepository

    private lateinit var inventarioService: InventarioService

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Central", codigoHogar = "HC-100")

    @BeforeEach
    fun setUp() {
        inventarioService = InventarioService(articuloRepository)
    }

    @Test
    fun `restarCantidad deduce unidades correctamente y retorna el DTO actualizado`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche Entera",
                fechaVencimiento = LocalDate.now().plusDays(5),
                cantidad = 10,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))
        `when`(articuloRepository.save(any(ArticuloDespensa::class.java))).thenAnswer { it.arguments[0] }

        val resultado = inventarioService.restarCantidad(articuloId, 4)

        assertEquals(6, resultado.cantidad)
        assertEquals(6, articulo.cantidad)
        verify(articuloRepository).save(articulo)
    }

    @Test
    fun `restarCantidad lanza CantidadInsuficienteException si la resta da saldo negativo`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Queso",
                fechaVencimiento = LocalDate.now().plusDays(10),
                cantidad = 3,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        assertThrows<CantidadInsuficienteException> {
            inventarioService.restarCantidad(articuloId, 5)
        }

        // Verifica que no se guarde ninguna modificación en la base de datos
        verify(articuloRepository, never()).save(any())
        assertEquals(3, articulo.cantidad)
    }

    @Test
    fun `eliminarArticulo lanza AccesoDenegadoException si el hogar del usuario no coincide con el del articulo`() {
        val articuloId = UUID.randomUUID()
        val hogarUsuarioDiferente = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Cereal",
                fechaVencimiento = LocalDate.now().plusMonths(2),
                cantidad = 2,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar, // Pertenece a hogarId, no a hogarUsuarioDiferente
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        assertThrows<AccesoDenegadoException> {
            inventarioService.eliminarArticulo(articuloId, hogarUsuarioDiferente)
        }

        // Verifica que el repositorio jamás invoque la eliminación
        verify(articuloRepository, never()).delete(any())
    }

    @Test
    fun `eliminarArticulo elimina exitosamente cuando los UUID de los hogares coinciden`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Cereal",
                fechaVencimiento = LocalDate.now().plusMonths(2),
                cantidad = 2,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        inventarioService.eliminarArticulo(articuloId, hogarId)

        verify(articuloRepository).delete(articulo)
    }

    @Test
    fun `obtenerInventarioPorHogar retorna la lista mapeada a ArticuloDTO`() {
        val articulo =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Arroz",
                fechaVencimiento = LocalDate.now().plusMonths(6),
                cantidad = 3,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )

        `when`(articuloRepository.findByHogarId(hogarId)).thenReturn(listOf(articulo))

        val lista = inventarioService.obtenerInventarioPorHogar(hogarId)

        assertEquals(1, lista.size)
        assertEquals("Arroz", lista[0].nombre)
        assertEquals(hogarId, lista[0].hogarId)
    }
}
