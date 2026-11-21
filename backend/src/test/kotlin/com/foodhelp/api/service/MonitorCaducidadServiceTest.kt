package com.foodhelp.api.service

import com.foodhelp.api.dto.RecetaExternaDTO
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class MonitorCaducidadServiceTest {
    @Mock
    private lateinit var articuloRepository: ArticuloRepository

    @Mock
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @Mock
    private lateinit var alertaRepository: AlertaRepository

    private val fixedInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val hoy = LocalDate.now(clock)

    private lateinit var monitorCaducidadService: MonitorCaducidadService

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Alertas", codigoHogar = "HA-01")

    private fun anyLocalDate(): LocalDate {
        any<LocalDate>()
        return LocalDate.now()
    }

    private fun anyCollection(): Collection<UUID> {
        any<Collection<UUID>>()
        return emptyList()
    }

    private fun captureList(captor: ArgumentCaptor<List<String>>): List<String> {
        captor.capture()
        return emptyList()
    }

    private fun captureAlertaList(captor: ArgumentCaptor<List<Alerta>>): List<Alerta> {
        captor.capture()
        return emptyList()
    }

    @BeforeEach
    fun setUp() {
        monitorCaducidadService =
            MonitorCaducidadService(
                articuloRepository,
                recetaIntegrationService,
                alertaRepository,
                clock,
            )
    }

    @Test
    fun `Test 1 - verifica que recetaIntegrationService es llamado con los ingredientes criticos correctos`() {
        val articulo1 =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Tomate",
                fechaVencimiento = hoy.plusDays(2),
                cantidad = 3,
                categoria = CategoriaEnum.VEGETALES,
                hogar = hogar,
            )
        val articulo2 =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Queso Mozzarella",
                fechaVencimiento = hoy.plusDays(4),
                cantidad = 1,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findPorVencerConHogar(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo2, articulo1))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(emptyList())

        val recetaMock =
            listOf(
                RecetaExternaDTO(
                    id = 101,
                    title = "Pizza Margarita",
                    image = "https://img.spoonacular.com/pizza.jpg",
                    missedIngredientCount = 1,
                ),
            )
        `when`(recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("Tomate", "Queso Mozzarella")))
            .thenReturn(recetaMock)

        monitorCaducidadService.ejecutarMonitoreo()

        verify(recetaIntegrationService).obtenerRecetasPorIngredientes(listOf("Tomate", "Queso Mozzarella"))
    }

    @Test
    fun `Test 2 - comprueba que la lista de ingredientes nunca supere el limite de 3 items cuando hay 10 productos`() {
        val articulos =
            (1..10).map { i ->
                ArticuloDespensa(
                    id = UUID.randomUUID(),
                    nombre = "Ingrediente $i",
                    fechaVencimiento = hoy.plusDays(i.toLong()),
                    cantidad = 1,
                    categoria = CategoriaEnum.OTROS,
                    hogar = hogar,
                )
            }

        `when`(articuloRepository.findPorVencerConHogar(anyLocalDate(), anyLocalDate()))
            .thenReturn(articulos)
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(emptyList())

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<String>>

        monitorCaducidadService.ejecutarMonitoreo()

        verify(recetaIntegrationService).obtenerRecetasPorIngredientes(captureList(captor))
        val ingredientesEnviados = captor.value

        assertEquals(3, ingredientesEnviados.size)
        assertEquals(listOf("Ingrediente 1", "Ingrediente 2", "Ingrediente 3"), ingredientesEnviados)
    }

    @Test
    fun `Test 3 - logica de frontera ignora productos cuando la consulta no devuelve articulos`() {
        `when`(articuloRepository.findPorVencerConHogar(anyLocalDate(), anyLocalDate()))
            .thenReturn(emptyList())

        val alertas = monitorCaducidadService.ejecutarMonitoreo()

        assertTrue(alertas.isEmpty(), "Si no hay productos por vencer no deben generarse alertas")
    }

    @Test
    fun `Test 4 - verifica que los productos por caducar se persisten como entidades Alerta en la base de datos`() {
        val articulo =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Yogur Griego",
                fechaVencimiento = hoy.plusDays(3),
                cantidad = 2,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findPorVencerConHogar(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(emptyList())

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Alerta>>

        monitorCaducidadService.ejecutarMonitoreo()

        verify(alertaRepository).saveAll(captureAlertaList(captor))
        val alertasPersistidas = captor.value

        assertEquals(1, alertasPersistidas.size)
        val alerta = alertasPersistidas.first()
        assertEquals(articulo.id, alerta.articuloId)
        assertEquals("Yogur Griego", alerta.nombreProducto)
        assertEquals(3, alerta.diasRestantes)
        assertEquals(hogarId, alerta.hogarId)
        assertFalse(alerta.leida)
        assertEquals(hoy, alerta.fechaCreacion)
    }

    @Test
    fun `Test 5 - idempotencia actualiza diasRestantes sin duplicar alerta si ya existe`() {
        val articuloId = UUID.randomUUID()
        val articulo =
            ArticuloDespensa(
                id = articuloId,
                nombre = "Leche",
                fechaVencimiento = hoy.plusDays(2),
                cantidad = 1,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )
        val alertaExistente =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = articuloId,
                nombreProducto = "Leche",
                diasRestantes = 3,
                hogarId = hogarId,
                fechaCreacion = hoy.minusDays(1),
            )

        `when`(articuloRepository.findPorVencerConHogar(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection()))
            .thenReturn(listOf(alertaExistente))

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Alerta>>

        val resultado = monitorCaducidadService.ejecutarMonitoreo()

        assertEquals(1, resultado.size)
        verify(alertaRepository).saveAll(captureAlertaList(captor))
        val alertasGuardadas = captor.value
        assertEquals(1, alertasGuardadas.size)
        assertEquals(alertaExistente.id, alertasGuardadas.first().id)
        assertEquals(2, alertasGuardadas.first().diasRestantes)
    }
}
