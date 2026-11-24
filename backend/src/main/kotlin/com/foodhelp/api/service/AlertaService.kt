package com.foodhelp.api.service

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.AlertaResponse
import com.foodhelp.api.exception.AlertaNotFoundException
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.repository.AlertaRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AlertaService(
    private val alertaRepository: AlertaRepository,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun listarPorHogar(
        hogarId: UUID,
        page: Int,
        size: Int,
    ): List<AlertaResponse> = listarPorHogar(hogarId, page, size, null)

    @Transactional(readOnly = true)
    fun listarPorHogar(
        hogarId: UUID,
        page: Int,
        size: Int,
        leida: Boolean?,
    ): List<AlertaResponse> {
        val pageable = Paginacion.de(page, size, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.asc("id")))
        val alertas =
            if (leida == null) {
                alertaRepository.findByHogarId(hogarId, pageable)
            } else {
                alertaRepository.findByHogarIdAndLeida(hogarId, leida, pageable)
            }
        return alertas.content.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun contarNoLeidas(hogarId: UUID): Long = alertaRepository.countByHogarIdAndLeidaFalse(hogarId)

    @Transactional
    fun marcarTodasComoLeidas(hogarId: UUID): Int = alertaRepository.marcarTodasComoLeidas(hogarId)

    @Transactional
    fun marcarComoLeida(
        alertaId: UUID,
        hogarId: UUID,
    ): AlertaResponse {
        val alerta =
            alertaRepository
                .findById(alertaId)
                .orElseThrow { AlertaNotFoundException(alertaId) }

        if (alerta.hogarId != hogarId) {
            throw AlertaNotFoundException(alertaId)
        }

        alerta.leida = true
        return alerta.toResponse()
    }

    @Transactional
    fun sincronizarDeArticulo(articulo: ArticuloDespensa) {
        val articuloId = checkNotNull(articulo.id) { "El artículo no está persistido (id es null)" }
        val hogarId = checkNotNull(articulo.hogar.id) { "El hogar del artículo no está persistido" }
        val fechaVencimiento =
            checkNotNull(articulo.fechaVencimiento) { "El artículo '${articulo.nombre}' no tiene fecha de vencimiento" }
        val hoy = LocalDate.now(clock)
        val diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento).toInt()
        val existente = alertaRepository.findByArticuloId(articuloId)

        if (articulo.cantidad <= 0 || diasRestantes > DIAS_ANTICIPACION) {
            existente?.let { alertaRepository.delete(it) }
            return
        }

        if (existente != null) {
            existente.diasRestantes = diasRestantes
            existente.leida = false
        } else {
            alertaRepository.save(
                Alerta(
                    articuloId = articuloId,
                    nombreProducto = articulo.nombre,
                    diasRestantes = diasRestantes,
                    hogarId = hogarId,
                    leida = false,
                    fechaCreacion = hoy,
                ),
            )
            Unit
        }
    }

    private fun Alerta.toResponse(): AlertaResponse =
        AlertaResponse(
            id = checkNotNull(id),
            articuloId = articuloId,
            nombreProducto = nombreProducto,
            diasRestantes = diasRestantes,
            leida = leida,
            fechaCreacion = fechaCreacion,
            vencido = diasRestantes < 0,
        )

    companion object {
        const val DIAS_ANTICIPACION = 10
    }
}
