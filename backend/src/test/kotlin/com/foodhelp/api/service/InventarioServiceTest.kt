package com.foodhelp.api.service

import com.foodhelp.api.dto.ActualizarArticuloRequest
import com.foodhelp.api.dto.CrearArticuloRequest
import com.foodhelp.api.exception.ArticuloNotFoundException
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.exception.CantidadInvalidaException
import com.foodhelp.api.exception.SolicitudInvalidaException
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InventarioServiceTest {
    @Mock
    private lateinit var articuloRepository: ArticuloRepository

    @Mock
    private lateinit var hogarRepository: HogarRepository

    @Mock
    private lateinit var alertaRepository: AlertaRepository

    private val fixedInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val hoy = LocalDate.now(clock)

    private lateinit var inventarioService: InventarioService

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Central", codigoHogar = "HC-100")

    private fun anyUUID(): UUID {
        any<UUID>()
        return UUID.randomUUID()
    }

    private fun anyArticulo(): ArticuloDespensa {
        any<ArticuloDespensa>()
        return ArticuloDespensa(
            id = UUID.randomUUID(),
            nombre = "dummy",
            fechaVencimiento = hoy,
            cantidad = 1,
            hogar = hogar,
        )
    }

    @BeforeEach
    fun setUp() {
        inventarioService = InventarioService(articuloRepository, hogarRepository, alertaRepository, clock)
    }

    @Test
    fun `restarCantidad deduce unidades correctamente y retorna el DTO actualizado`() {
        val articuloId = UUID.randomUUID()
        val articuloOriginal =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche Entera",
                fechaVencimiento = hoy.plusDays(5),
                cantidad = 10,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )
        val articuloActualizado =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche Entera",
                fechaVencimiento = hoy.plusDays(5),
                cantidad = 6,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId))
            .thenReturn(Optional.of(articuloOriginal))
            .thenReturn(Optional.of(articuloActualizado))
        `when`(articuloRepository.restarCantidadAtomica(articuloId, 4)).thenReturn(1)

        val resultado = inventarioService.restarCantidad(articuloId, hogarId, 4)

        assertEquals(6, resultado.cantidad)
        verify(articuloRepository).restarCantidadAtomica(articuloId, 4)
    }

    @Test
    fun `restarCantidad lanza CantidadInvalidaException si la cantidad a restar es menor o igual a cero`() {
        val articuloId = UUID.randomUUID()

        assertThrows<CantidadInvalidaException> {
            inventarioService.restarCantidad(articuloId, hogarId, 0)
        }
        assertThrows<CantidadInvalidaException> {
            inventarioService.restarCantidad(articuloId, hogarId, -5)
        }
        verify(articuloRepository, never()).restarCantidadAtomica(anyUUID(), anyInt())
    }

    @Test
    fun `restarCantidad lanza ArticuloNotFoundException si el articulo no pertenece al hogar para prevenir enumeracion`() {
        val articuloId = UUID.randomUUID()
        val otroHogarId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche",
                fechaVencimiento = hoy.plusDays(5),
                cantidad = 10,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        assertThrows<ArticuloNotFoundException> {
            inventarioService.restarCantidad(articuloId, otroHogarId, 2)
        }
        verify(articuloRepository, never()).restarCantidadAtomica(anyUUID(), anyInt())
    }

    @Test
    fun `restarCantidad lanza CantidadInsuficienteException si restarCantidadAtomica devuelve cero filas afectadas`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Queso",
                fechaVencimiento = hoy.plusDays(10),
                cantidad = 3,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))
        `when`(articuloRepository.restarCantidadAtomica(articuloId, 5)).thenReturn(0)

        assertThrows<CantidadInsuficienteException> {
            inventarioService.restarCantidad(articuloId, hogarId, 5)
        }
    }

    @Test
    fun `eliminarArticulo lanza ArticuloNotFoundException si el hogar no coincide (anti-enumeracion)`() {
        val articuloId = UUID.randomUUID()
        val hogarUsuarioDiferente = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Cereal",
                fechaVencimiento = hoy.plusMonths(2),
                cantidad = 2,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )

        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        assertThrows<ArticuloNotFoundException> {
            inventarioService.eliminarArticulo(articuloId, hogarUsuarioDiferente)
        }

        verify(articuloRepository, never()).delete(anyArticulo())
    }

    @Test
    fun `eliminarArticulo elimina exitosamente cuando los UUID de los hogares coinciden`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Cereal",
                fechaVencimiento = hoy.plusMonths(2),
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
                fechaVencimiento = hoy.plusMonths(6),
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

    @Test
    fun `obtenerInventarioVigente filtra articulos con stock mayor a cero y no vencidos`() {
        val valido =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Arroz",
                fechaVencimiento = hoy.plusDays(5),
                cantidad = 2,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )
        val vencido =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Leche",
                fechaVencimiento = hoy.minusDays(1),
                cantidad = 2,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )
        val agotado =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Pan",
                fechaVencimiento = hoy.plusDays(2),
                cantidad = 0,
                categoria = CategoriaEnum.PANADERIA,
                hogar = hogar,
            )

        `when`(articuloRepository.findByHogarId(hogarId)).thenReturn(listOf(valido, vencido, agotado))

        val vigentes = inventarioService.obtenerInventarioVigente(hogarId)

        assertEquals(1, vigentes.size)
        assertEquals("Arroz", vigentes[0].nombre)
    }

    @Test
    fun `crearArticulo guarda y retorna el nuevo articulo`() {
        val request =
            CrearArticuloRequest(
                nombre = "Manzanas",
                fechaVencimiento = hoy.plusDays(10),
                cantidad = 5,
                categoria = CategoriaEnum.FRUTAS,
            )
        `when`(hogarRepository.findById(hogarId)).thenReturn(Optional.of(hogar))
        `when`(articuloRepository.save(anyArticulo())).thenAnswer { invocation ->
            val art = invocation.arguments[0] as ArticuloDespensa
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = art.nombre,
                fechaVencimiento = art.fechaVencimiento,
                cantidad = art.cantidad,
                categoria = art.categoria,
                hogar = art.hogar,
            )
        }

        val creado = inventarioService.crearArticulo(hogarId, request)

        assertEquals("Manzanas", creado.nombre)
        assertEquals(5, creado.cantidad)
        assertEquals(hogarId, creado.hogarId)
    }

    @Test
    fun `crearArticulo rechaza fecha de vencimiento anterior a hoy con Clock fijo`() {
        val request =
            CrearArticuloRequest(
                nombre = "Leche",
                fechaVencimiento = hoy.minusDays(1),
                cantidad = 2,
            )

        `when`(hogarRepository.findById(hogarId)).thenReturn(Optional.of(hogar))

        val excepcion =
            assertThrows<SolicitudInvalidaException> {
                inventarioService.crearArticulo(hogarId, request)
            }

        assertEquals("La fecha de vencimiento no puede ser anterior a hoy", excepcion.message)
        verify(articuloRepository, never()).save(anyArticulo())
    }

    @Test
    fun `crearArticulo acepta fecha de vencimiento igual a hoy con Clock fijo`() {
        val request =
            CrearArticuloRequest(
                nombre = "Leche",
                fechaVencimiento = hoy,
                cantidad = 2,
            )
        `when`(hogarRepository.findById(hogarId)).thenReturn(Optional.of(hogar))
        `when`(articuloRepository.save(anyArticulo())).thenAnswer { invocation ->
            val art = invocation.arguments[0] as ArticuloDespensa
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = art.nombre,
                fechaVencimiento = art.fechaVencimiento,
                cantidad = art.cantidad,
                categoria = art.categoria,
                hogar = art.hogar,
            )
        }

        val creado = inventarioService.crearArticulo(hogarId, request)

        assertEquals(hoy, creado.fechaVencimiento)
    }

    @Test
    fun `actualizarArticulo rechaza fecha de vencimiento anterior a hoy y no modifica el articulo`() {
        val articuloId = UUID.randomUUID()
        val fechaOriginal = hoy.plusDays(5)
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche",
                fechaVencimiento = fechaOriginal,
                cantidad = 4,
                hogar = hogar,
            )
        `when`(articuloRepository.findById(articuloId)).thenReturn(Optional.of(articulo))

        assertThrows<SolicitudInvalidaException> {
            inventarioService.actualizarArticulo(
                articuloId,
                hogarId,
                ActualizarArticuloRequest(fechaVencimiento = hoy.minusDays(1)),
            )
        }

        assertEquals(fechaOriginal, articulo.fechaVencimiento)
    }
}
