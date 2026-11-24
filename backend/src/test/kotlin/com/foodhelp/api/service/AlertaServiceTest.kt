package com.foodhelp.api.service

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.AlertaResponse
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.AlertaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AlertaServiceTest {
    @Mock
    private lateinit var alertaRepository: AlertaRepository

    private val fixedInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val hoy = LocalDate.now(clock)

    private val hogarId = UUID.randomUUID()
    private val hogar = Hogar(id = hogarId, nombre = "Hogar Central", codigoHogar = "HC-100")

    private lateinit var alertaService: AlertaService

    @BeforeEach
    fun setUp() {
        alertaService = AlertaService(alertaRepository, clock)
    }

    private fun articulo(
        id: UUID,
        diasParaVencer: Int,
        cantidad: Int = 3,
    ) = ArticuloDespensa(
        id = id,
        nombre = "Leche",
        fechaVencimiento = hoy.plusDays(diasParaVencer.toLong()),
        cantidad = cantidad,
        hogar = hogar,
    )

    private fun capturarAlertaGuardada(): Alerta {
        val captor = ArgumentCaptor.forClass(Alerta::class.java)
        verify(alertaRepository).save(captor.capture())
        return captor.value
    }

    @Test
    fun `sincronizarDeArticulo crea alerta no leida si vence dentro de los diez dias`() {
        val articulo = articulo(UUID.randomUUID(), 3)
        `when`(alertaRepository.findByArticuloId(articulo.id!!)).thenReturn(null)

        alertaService.sincronizarDeArticulo(articulo)

        val guardada = capturarAlertaGuardada()
        assertEquals(articulo.id, guardada.articuloId)
        assertEquals("Leche", guardada.nombreProducto)
        assertEquals(3, guardada.diasRestantes)
        assertEquals(hogarId, guardada.hogarId)
        assertEquals(hoy, guardada.fechaCreacion)
        assertFalse(guardada.leida)
    }

    @Test
    fun `sincronizarDeArticulo actualiza dias y marca como no leida una alerta existente`() {
        val articulo = articulo(UUID.randomUUID(), 3)
        val existente =
            Alerta(
                articuloId = articulo.id!!,
                nombreProducto = "Leche",
                diasRestantes = 8,
                hogarId = hogarId,
                leida = true,
                fechaCreacion = hoy,
            )
        `when`(alertaRepository.findByArticuloId(articulo.id!!)).thenReturn(existente)

        alertaService.sincronizarDeArticulo(articulo)

        assertEquals(3, existente.diasRestantes)
        assertFalse(existente.leida)
        verify(alertaRepository, never()).save(org.mockito.ArgumentMatchers.any(Alerta::class.java))
    }

    @Test
    fun `sincronizarDeArticulo elimina la alerta si el articulo queda a mas de diez dias`() {
        val articulo = articulo(UUID.randomUUID(), 11)
        val existente =
            Alerta(
                articuloId = articulo.id!!,
                nombreProducto = "Leche",
                diasRestantes = 2,
                hogarId = hogarId,
                leida = false,
                fechaCreacion = hoy,
            )
        `when`(alertaRepository.findByArticuloId(articulo.id!!)).thenReturn(existente)

        alertaService.sincronizarDeArticulo(articulo)

        verify(alertaRepository).delete(existente)
        verify(alertaRepository, never()).save(org.mockito.ArgumentMatchers.any(Alerta::class.java))
    }

    @Test
    fun `sincronizarDeArticulo no crea alerta para un articulo agotado`() {
        val articulo = articulo(UUID.randomUUID(), 3, cantidad = 0)
        `when`(alertaRepository.findByArticuloId(articulo.id!!)).thenReturn(null)

        alertaService.sincronizarDeArticulo(articulo)

        verify(alertaRepository, never()).save(org.mockito.ArgumentMatchers.any(Alerta::class.java))
    }

    private fun respuestaConDias(diasRestantes: Int): AlertaResponse {
        val alerta =
            Alerta(
                id = UUID.randomUUID(),
                articuloId = UUID.randomUUID(),
                nombreProducto = "Leche",
                diasRestantes = diasRestantes,
                hogarId = hogarId,
                leida = false,
                fechaCreacion = hoy,
            )
        val pageable =
            Paginacion.de(0, 10, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.asc("id")))
        `when`(alertaRepository.findByHogarId(hogarId, pageable)).thenReturn(PageImpl(listOf(alerta)))
        return alertaService.listarPorHogar(hogarId, 0, 10).single()
    }

    @Test
    fun `vencido es falso cuando faltan dias para vencer`() {
        assertEquals(false, respuestaConDias(2).vencido)
    }

    @Test
    fun `vencido es falso cuando vence hoy`() {
        assertEquals(false, respuestaConDias(0).vencido)
    }

    @Test
    fun `vencido es verdadero cuando la fecha ya paso`() {
        assertEquals(true, respuestaConDias(-1).vencido)
    }
}
