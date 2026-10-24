package com.foodhelp.api.service

import com.foodhelp.api.dto.CreateHogarRequest
import com.foodhelp.api.dto.HogarResponse
import com.foodhelp.api.dto.JoinHogarRequest
import com.foodhelp.api.dto.UsuarioResponse
import com.foodhelp.api.exception.HogarNotFoundException
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.HogarRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class HogarService(
    private val hogarRepository: HogarRepository,
) {
    fun crearHogar(request: CreateHogarRequest): HogarResponse {
        val codigo =
            UUID
                .randomUUID()
                .toString()
                .take(8)
                .uppercase()
        val hogar = Hogar(nombre = request.nombre, codigoHogar = codigo)
        return hogarRepository.save(hogar).toResponse()
    }

    @Transactional(readOnly = true)
    fun unirseAHogar(request: JoinHogarRequest): HogarResponse {
        val hogar =
            hogarRepository.findByCodigoHogar(request.codigoHogar)
                ?: throw HogarNotFoundException(request.codigoHogar)
        return hogar.toResponse()
    }

    @Transactional(readOnly = true)
    fun obtenerHogar(codigoHogar: String): HogarResponse {
        val hogar =
            hogarRepository.findByCodigoHogar(codigoHogar)
                ?: throw HogarNotFoundException(codigoHogar)
        return hogar.toResponse()
    }

    private fun Hogar.toResponse(): HogarResponse =
        HogarResponse(
            id = id!!,
            nombre = nombre,
            codigoHogar = codigoHogar,
            // usuarios se accede dentro de @Transactional para evitar LazyInitializationException
            usuarios = usuarios.map { UsuarioResponse(id = it.id!!, nombre = it.nombre) },
        )
}
