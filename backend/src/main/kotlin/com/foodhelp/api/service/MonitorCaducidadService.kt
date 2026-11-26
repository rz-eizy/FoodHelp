package com.foodhelp.api.service

import com.foodhelp.api.dto.AlertaDTO
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class MonitorCaducidadService(
    private val articuloRepository: ArticuloRepository,
    private val alertaRepository: AlertaRepository,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(MonitorCaducidadService::class.java)

    // El cron y el cálculo de fechas comparten la zona configurable; la JVM corre en UTC
    @Scheduled(cron = "\${app.monitor.cron:0 0 8 * * ?}", zone = "\${app.zona-horaria:America/Santiago}")
    fun ejecutarMonitoreo(): List<AlertaDTO> {
        val hoy = LocalDate.now(clock)
        val fechaLimite = hoy.plusDays(AlertaService.DIAS_ANTICIPACION.toLong())

        log.info("Iniciando monitoreo diario de caducidad hasta {}", fechaLimite)

        // Incluye vencidos: mientras haya stock, la alerta debe seguir viva con días negativos
        val articulos = articuloRepository.findConAlertaActiva(fechaLimite)
        if (articulos.isEmpty()) return emptyList()

        // Una alerta por artículo: se actualiza la existente en lugar de duplicarla
        val existentes =
            alertaRepository
                .findByArticuloIdIn(articulos.mapNotNull { it.id })
                .associateBy { it.articuloId }

        val alertas =
            articulos.mapNotNull { articulo ->
                val articuloId = articulo.id ?: return@mapNotNull null
                val vencimiento = articulo.fechaVencimiento ?: return@mapNotNull null
                val dias = ChronoUnit.DAYS.between(hoy, vencimiento).toInt()

                existentes[articuloId]?.apply {
                    val diasAnteriores = diasRestantes
                    diasRestantes = dias
                    if (dias < 0 && diasAnteriores >= 0) leida = false
                }
                    ?: Alerta(
                        articuloId = articuloId,
                        nombreProducto = articulo.nombre,
                        diasRestantes = dias,
                        hogarId = checkNotNull(articulo.hogar.id),
                        fechaCreacion = hoy,
                        leida = false,
                    )
            }
        alertaRepository.saveAll(alertas)

        val alertasDto =
            alertas.map {
                AlertaDTO(
                    articuloId = it.articuloId,
                    nombreProducto = it.nombreProducto,
                    diasRestantes = it.diasRestantes.toLong(),
                    hogarId = it.hogarId,
                )
            }
        alertasDto.forEach {
            log.info(
                "ALERTA CADUCIDAD: El producto '{}' vence en {} días.",
                it.nombreProducto,
                it.diasRestantes,
            )
        }
        return alertasDto
    }
}
