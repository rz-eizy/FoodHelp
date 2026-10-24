package com.foodhelp.api.service

import com.foodhelp.api.dto.ArticuloDTO
import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.ArticuloNotFoundException
import com.foodhelp.api.exception.CantidadInsuficienteException
import com.foodhelp.api.repository.ArticuloRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InventarioService(
    private val articuloRepository: ArticuloRepository,
) {
    @Transactional(readOnly = true)
    fun obtenerInventarioPorHogar(hogarId: UUID): List<ArticuloDTO> =
        articuloRepository.findByHogarId(hogarId).map { ArticuloDTO.fromEntity(it) }

    @Transactional
    fun restarCantidad(
        articuloId: UUID,
        cantidadARestar: Int,
    ): ArticuloDTO {
        val articulo =
            articuloRepository
                .findById(articuloId)
                .orElseThrow { ArticuloNotFoundException(articuloId) }

        val nuevaCantidad = articulo.cantidad - cantidadARestar
        if (nuevaCantidad < 0) {
            throw CantidadInsuficienteException(
                "La deducción de $cantidadARestar unidades excede el stock actual (${articulo.cantidad})",
            )
        }

        articulo.cantidad = nuevaCantidad
        val guardado = articuloRepository.save(articulo)
        return ArticuloDTO.fromEntity(guardado)
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
            throw AccesoDenegadoException(
                "Acceso Denegado: el artículo no pertenece al hogar especificado ($hogarIdUsuario)",
            )
        }

        articuloRepository.delete(articulo)
    }
}
