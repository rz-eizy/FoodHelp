package com.foodhelp.api.security

import com.foodhelp.api.exception.AccesoDenegadoException
import com.foodhelp.api.exception.CodigoError
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.UsuarioRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UsuarioActualService(
    private val usuarioRepository: UsuarioRepository,
) {
    private fun correoAutenticado(): String =
        SecurityContextHolder.getContext().authentication?.name
            ?: throw AccesoDenegadoException("No se encontró información de autenticación", CodigoError.ACCESO_DENEGADO)

    @Transactional(readOnly = true)
    fun usuarioActual(): Usuario =
        usuarioRepository.findByCorreo(correoAutenticado())
            ?: throw AccesoDenegadoException("Usuario autenticado no encontrado en el sistema", CodigoError.ACCESO_DENEGADO)

    @Transactional(readOnly = true)
    fun hogarIdActual(): UUID =
        usuarioActual().hogar?.id
            ?: throw AccesoDenegadoException("El usuario autenticado no está asociado a ningún hogar", CodigoError.SIN_HOGAR)

    @Transactional(readOnly = true)
    fun exigirPertenenciaAHogar(hogarId: UUID) {
        if (hogarIdActual() != hogarId) {
            throw AccesoDenegadoException("Acceso Denegado: el recurso no pertenece a tu hogar", CodigoError.HOGAR_AJENO)
        }
    }
}
