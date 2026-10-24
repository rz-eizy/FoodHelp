package com.foodhelp.api.service

import com.foodhelp.api.dto.AlertaDTO
import com.foodhelp.api.models.Alerta
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class MonitorCaducidadService(
    private val articuloRepository: ArticuloRepository,
    private val recetaIntegrationService: RecetaIntegrationService,
    private val alertaRepository: AlertaRepository,
) {
    private val log = LoggerFactory.getLogger(MonitorCaducidadService::class.java)

    @Scheduled(cron = "0 0 8 * * ?")
    fun ejecutarMonitoreo(): List<AlertaDTO> {
        val hoy = LocalDate.now()
        val fechaLimite = hoy.plusDays(10)

        log.info("Iniciando monitoreo diario de caducidad para articulos entre $hoy y $fechaLimite")

        val articulosPorVencer = articuloRepository.findByFechaVencimientoBetween(hoy, fechaLimite)

        val articulosValidos =
            articulosPorVencer.filter { articulo ->
                val fecha = articulo.fechaVencimiento ?: return@filter false
                val dias = ChronoUnit.DAYS.between(hoy, fecha)
                dias in 0..10
            }

        val alertasEntidades =
            articulosValidos.map { articulo ->
                val fecha = articulo.fechaVencimiento!!
                val diasRestantes = ChronoUnit.DAYS.between(hoy, fecha).toInt()
                Alerta(
                    articuloId = articulo.id!!,
                    nombreProducto = articulo.nombre,
                    diasRestantes = diasRestantes,
                    hogarId = articulo.hogar.id!!,
                    fechaCreacion = hoy,
                    leida = false,
                )
            }

        if (alertasEntidades.isNotEmpty()) {
            alertaRepository.saveAll(alertasEntidades)
        }

        val alertas =
            articulosValidos.map { articulo ->
                val fecha = articulo.fechaVencimiento!!
                val diasRestantes = ChronoUnit.DAYS.between(hoy, fecha)
                AlertaDTO(
                    articuloId = articulo.id!!,
                    nombreProducto = articulo.nombre,
                    diasRestantes = diasRestantes,
                    hogarId = articulo.hogar.id!!,
                )
            }

        alertas.forEach { alerta ->
            log.warn(
                "ALERTA CADUCIDAD: El producto '{}' (ID: {}) del hogar {} vence en {} días.",
                alerta.nombreProducto,
                alerta.articuloId,
                alerta.hogarId,
                alerta.diasRestantes,
            )
        }

        val articulosPorHogar = articulosValidos.groupBy { it.hogar.id!! }

        articulosPorHogar.forEach { (hogarId, articulosDelHogar) ->
            val ingredientesCriticos =
                articulosDelHogar
                    .sortedBy { it.fechaVencimiento }
                    .take(3)
                    .map { it.nombre }

            val recetasSugeridas = recetaIntegrationService.obtenerRecetasPorIngredientes(ingredientesCriticos)
            val titulosRecetas = recetasSugeridas.map { it.title }

            log.info(
                "Hogar ID: {} | Ingredientes críticos: {} | Recetas sugeridas: {}",
                hogarId,
                ingredientesCriticos,
                titulosRecetas,
            )
        }

        return alertas
    }
}
