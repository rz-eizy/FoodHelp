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
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class MonitorCaducidadServiceTest {
    @Mock
    private lateinit var articuloRepository: ArticuloRepository

    @Mock
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @Mock
    private lateinit var alertaRepository: AlertaRepository

    private lateinit var monitorCaducidadService: MonitorCaducidadService

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Alertas", codigoHogar = "HA-01")

    private fun anyLocalDate(): LocalDate {
        any<LocalDate>()
        return LocalDate.now()
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
        monitorCaducidadService = MonitorCaducidadService(articuloRepository, recetaIntegrationService, alertaRepository)
    }

    @Test
    fun `Test 1 - verifica que recetaIntegrationService es llamado con los ingredientes criticos correctos`() {
        val hoy = LocalDate.now()
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

        // Pasamos en orden inverso de vencimiento para comprobar ordenamiento
        `when`(articuloRepository.findByFechaVencimientoBetween(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo2, articulo1))

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
        val hoy = LocalDate.now()
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

        `when`(articuloRepository.findByFechaVencimientoBetween(anyLocalDate(), anyLocalDate()))
            .thenReturn(articulos)

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<String>>

        monitorCaducidadService.ejecutarMonitoreo()

        verify(recetaIntegrationService).obtenerRecetasPorIngredientes(captureList(captor))
        val ingredientesEnviados = captor.value

        assertEquals(3, ingredientesEnviados.size)
        assertEquals(listOf("Ingrediente 1", "Ingrediente 2", "Ingrediente 3"), ingredientesEnviados)
    }

    @Test
    fun `Test 3 - logica de frontera ignora productos que vencen en mas de 10 dias`() {
        val hoy = LocalDate.now()
        val articulo30Dias =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Arroz Integral",
                fechaVencimiento = hoy.plusDays(30),
                cantidad = 5,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )

        `when`(articuloRepository.findByFechaVencimientoBetween(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo30Dias))

        val alertas = monitorCaducidadService.ejecutarMonitoreo()

        assertTrue(alertas.isEmpty(), "Los productos con más de 10 días no deben procesarse")
    }

    @Test
    fun `Test 4 - verifica que los productos por caducar se persisten como entidades Alerta en la base de datos`() {
        val hoy = LocalDate.now()
        val articulo =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Yogur Griego",
                fechaVencimiento = hoy.plusDays(3),
                cantidad = 2,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        `when`(articuloRepository.findByFechaVencimientoBetween(anyLocalDate(), anyLocalDate()))
            .thenReturn(listOf(articulo))

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
}
