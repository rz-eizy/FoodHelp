package com.foodhelp.api.service

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.AlertaResponse
import com.foodhelp.api.exception.AlertaNotFoundException
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.repository.AlertaRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AlertaService(
    private val alertaRepository: AlertaRepository,
) {
    @Transactional(readOnly = true)
    fun listarPorHogar(
        hogarId: UUID,
        page: Int,
        size: Int,
    ): List<AlertaResponse> {
        val pageable = Paginacion.de(page, size, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.asc("id")))
        return alertaRepository.findByHogarId(hogarId, pageable).content.map { it.toResponse() }
    }

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

    private fun Alerta.toResponse(): AlertaResponse =
        AlertaResponse(
            id = checkNotNull(id),
            articuloId = articuloId,
            nombreProducto = nombreProducto,
            diasRestantes = diasRestantes,
            leida = leida,
            fechaCreacion = fechaCreacion,
        )
}
