package com.foodhelp.api.service

import com.foodhelp.api.config.Paginacion
import com.foodhelp.api.dto.ActualizarArticuloRequest
import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.dto.CrearArticuloRequest
import com.foodhelp.api.exception.ArticuloNotFoundException
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.exception.CantidadInvalidaException
import com.foodhelp.api.exception.HogarNotFoundException
import com.foodhelp.api.exception.SolicitudInvalidaException
import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.repository.AlertaRepository
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class InventarioService(
    private val articuloRepository: ArticuloRepository,
    private val hogarRepository: HogarRepository,
    private val alertaRepository: AlertaRepository,
    private val alertaService: AlertaService,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun obtenerInventarioPorHogar(hogarId: UUID): List<ArticuloDTO> =
        articuloRepository.findByHogarId(hogarId).map { ArticuloDTO.fromEntity(it) }

    @Transactional(readOnly = true)
    fun obtenerInventarioVigente(hogarId: UUID): List<ArticuloDTO> {
        val hoy = LocalDate.now(clock)
        return obtenerInventarioPorHogar(hogarId)
            .filter { it.cantidad > 0 && !it.fechaVencimiento.isBefore(hoy) }
    }

    @Transactional(readOnly = true)
    fun obtenerInventarioPaginado(
        hogarId: UUID,
        page: Int,
        size: Int,
    ): List<ArticuloDTO> {
        val pageable = Paginacion.de(page, size, Sort.by("fechaVencimiento", "id"))
        return articuloRepository.findByHogarId(hogarId, pageable).content.map { ArticuloDTO.fromEntity(it) }
    }

    @Transactional
    fun crearArticulo(
        hogarId: UUID,
        request: CrearArticuloRequest,
    ): ArticuloDTO {
        val hogar =
            hogarRepository
                .findById(hogarId)
                .orElseThrow { HogarNotFoundException(hogarId.toString()) }

        val fechaVencimiento =
            checkNotNull(request.fechaVencimiento) { "La fecha de vencimiento es obligatoria" }
        validarFechaVencimiento(fechaVencimiento)

        val articulo =
            ArticuloDespensa(
                nombre = request.nombre.trim(),
                fechaVencimiento = fechaVencimiento,
                cantidad = request.cantidad,
                categoria = request.categoria,
                hogar = hogar,
            )
        val guardado = articuloRepository.save(articulo)
        alertaService.sincronizarDeArticulo(guardado)
        return ArticuloDTO.fromEntity(guardado)
    }

    @Transactional
    fun restarCantidad(
        articuloId: UUID,
        hogarId: UUID,
        cantidadARestar: Int,
    ): ArticuloDTO {
        if (cantidadARestar <= 0) throw CantidadInvalidaException()

        val articulo =
            articuloRepository
                .findById(articuloId)
                .orElseThrow { ArticuloNotFoundException(articuloId) }

        if (articulo.hogar.id != hogarId) {
            throw ArticuloNotFoundException(articuloId)
        }

        val filasAfectadas = articuloRepository.restarCantidadAtomica(articuloId, cantidadARestar)
        if (filasAfectadas == 0) {
            throw CantidadInsuficienteException(
                "La deducción de $cantidadARestar unidades excede el stock disponible",
            )
        }

        val actualizado =
            articuloRepository
                .findById(articuloId)
                .orElseThrow { ArticuloNotFoundException(articuloId) }
        val respuesta = ArticuloDTO.fromEntity(actualizado)
        if (actualizado.cantidad == 0) {
            eliminarArticuloAgotado(actualizado)
        }
        return respuesta
    }

    @Transactional
    fun eliminarArticulo(
        articuloId: UUID,
        hogarIdUsuario: UUID,
    ) {
        val articulo =
            articuloRepository
                .findById(articuloId)
                .orElseThrow { ArticuloNotFoundException(articuloId) }

        if (articulo.hogar.id != hogarIdUsuario) {
            throw ArticuloNotFoundException(articuloId)
        }
        eliminarArticuloAgotado(articulo)
    }

    @Transactional
    fun actualizarArticulo(
        articuloId: UUID,
        hogarId: UUID,
        request: ActualizarArticuloRequest,
    ): ArticuloDTO {
        if (request.nombre == null && request.fechaVencimiento == null && request.cantidad == null && request.categoria == null) {
            throw SolicitudInvalidaException("Debes enviar al menos un campo para actualizar")
        }

        val articulo =
            articuloRepository
                .findById(articuloId)
                .orElseThrow { ArticuloNotFoundException(articuloId) }

        if (articulo.hogar.id != hogarId) {
            throw ArticuloNotFoundException(articuloId)
        }

        request.fechaVencimiento?.let { validarFechaVencimiento(it) }

        if (request.cantidad == 0) {
            val respuesta = ArticuloDTO.fromEntity(articulo).copy(cantidad = 0)
            eliminarArticuloAgotado(articulo)
            return respuesta
        }

        request.nombre?.let { articulo.nombre = it.trim() }
        request.cantidad?.let { articulo.cantidad = it }
        request.categoria?.let { articulo.categoria = it }
        if (request.fechaVencimiento != null) {
            articulo.fechaVencimiento = request.fechaVencimiento
            alertaService.sincronizarDeArticulo(articulo)
        }

        return ArticuloDTO.fromEntity(articulo)
    }

    private fun eliminarArticuloAgotado(articulo: ArticuloDespensa) {
        articulo.id?.let { alertaRepository.deleteByArticuloId(it) }
        articuloRepository.delete(articulo)
    }

    private fun validarFechaVencimiento(fechaVencimiento: LocalDate) {
        if (fechaVencimiento.isBefore(LocalDate.now(clock))) {
            throw SolicitudInvalidaException("La fecha de vencimiento no puede ser anterior a hoy")
        }
    }
}
