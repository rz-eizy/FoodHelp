package com.foodhelp.api.service

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
import org.mockito.Mockito.times
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
    private lateinit var alertaRepository: AlertaRepository

    private val fixedInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val hoy = LocalDate.now(clock)

    private lateinit var monitorCaducidadService: MonitorCaducidadService

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Alertas", codigoHogar = "HA-01")

    private fun anyLocalDate(): LocalDate {
        any<LocalDate>()
        return hoy.plusDays(AlertaService.DIAS_ANTICIPACION.toLong())
    }

    private fun anyCollection(): Collection<UUID> {
        any<Collection<UUID>>()
        return emptyList()
    }

    private fun capturarAlertas(captor: ArgumentCaptor<List<Alerta>>): List<Alerta> {
        captor.capture()
        return emptyList()
    }

    private fun articuloVencido(dias: Int) =
        ArticuloDespensa(
            id = UUID.randomUUID(),
            nombre = "Yogur Griego",
            fechaVencimiento = hoy.plusDays(dias.toLong()),
            cantidad = 2,
            categoria = CategoriaEnum.LACTEOS,
            hogar = hogar,
        )

    @BeforeEach
    fun setUp() {
        monitorCaducidadService = MonitorCaducidadService(articuloRepository, alertaRepository, clock)
    }

    @Test
    fun `Test 1 - producto vencido conserva su alerta con dias negativos y el valor baja en otra ejecucion`() {
        val articulo = articuloVencido(-1)
        `when`(articuloRepository.findConAlertaActiva(anyLocalDate())).thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(emptyList())

        val primera = monitorCaducidadService.ejecutarMonitoreo()

        assertEquals(-1L, primera.single().diasRestantes)

        val clockPosterior = Clock.fixed(fixedInstant.plus(1, java.time.temporal.ChronoUnit.DAYS), ZoneId.of("UTC"))
        val monitorPosterior = MonitorCaducidadService(articuloRepository, alertaRepository, clockPosterior)
        val alertaExistente =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = articulo.id!!,
                nombreProducto = articulo.nombre,
                diasRestantes = -1,
                hogarId = hogarId,
                fechaCreacion = hoy,
            )
        `when`(alertaRepository.findByArticuloIdIn(listOf(articulo.id!!))).thenReturn(listOf(alertaExistente))

        val segunda = monitorPosterior.ejecutarMonitoreo()

        assertEquals(-2L, segunda.single().diasRestantes)
        verify(alertaRepository, times(2)).saveAll(any<List<Alerta>>())
    }

    @Test
    fun `Test 2 - al vencer una alerta leida se marca como no leida`() {
        val articulo = articuloVencido(-1)
        val alertaExistente =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = articulo.id!!,
                nombreProducto = articulo.nombre,
                diasRestantes = 1,
                hogarId = hogarId,
                leida = true,
                fechaCreacion = hoy.minusDays(2),
            )
        `when`(articuloRepository.findConAlertaActiva(anyLocalDate())).thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(listOf(alertaExistente))

        monitorCaducidadService.ejecutarMonitoreo()

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Alerta>>
        verify(alertaRepository).saveAll(capturarAlertas(captor))
        val guardada = captor.value.single()
        assertEquals(alertaExistente.id, guardada.id)
        assertEquals(-1, guardada.diasRestantes)
        assertFalse(guardada.leida)
    }

    @Test
    fun `Test 3 - idempotencia actualiza diasRestantes sin duplicar alerta si ya existe`() {
        val articulo =
            ArticuloDespensa(
                id = UUID.randomUUID(),
                nombre = "Leche",
                fechaVencimiento = hoy.plusDays(2),
                cantidad = 1,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )
        val alertaExistente =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = articulo.id!!,
                nombreProducto = "Leche",
                diasRestantes = 3,
                hogarId = hogarId,
                fechaCreacion = hoy.minusDays(1),
            )
        `when`(articuloRepository.findConAlertaActiva(anyLocalDate())).thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(listOf(alertaExistente))

        val resultado = monitorCaducidadService.ejecutarMonitoreo()

        assertEquals(1, resultado.size)

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Alerta>>
        verify(alertaRepository).saveAll(capturarAlertas(captor))
        val guardadas = captor.value
        assertEquals(1, guardadas.size)
        assertEquals(alertaExistente.id, guardadas.first().id)
        assertEquals(2, guardadas.first().diasRestantes)
        assertTrue(guardadas.first().leida.not())
    }

    @Test
    fun `Test 4 - consulta hasta 10 dias sin limite inferior y sin articulos agotados`() {
        `when`(articuloRepository.findConAlertaActiva(hoy.plusDays(10))).thenReturn(emptyList())

        val alertas = monitorCaducidadService.ejecutarMonitoreo()

        assertTrue(alertas.isEmpty())
        verify(articuloRepository).findConAlertaActiva(hoy.plusDays(10))
    }

    @Test
    fun `Test 5 - producto por caducar se persiste como entidad Alerta no leida`() {
        val articulo = articuloVencido(3)
        `when`(articuloRepository.findConAlertaActiva(anyLocalDate())).thenReturn(listOf(articulo))
        `when`(alertaRepository.findByArticuloIdIn(anyCollection())).thenReturn(emptyList())

        monitorCaducidadService.ejecutarMonitoreo()

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Alerta>>
        verify(alertaRepository).saveAll(capturarAlertas(captor))
        val alerta = captor.value.single()
        assertEquals(articulo.id, alerta.articuloId)
        assertEquals("Yogur Griego", alerta.nombreProducto)
        assertEquals(3, alerta.diasRestantes)
        assertEquals(hogarId, alerta.hogarId)
        assertFalse(alerta.leida)
        assertEquals(hoy, alerta.fechaCreacion)
    }
}
