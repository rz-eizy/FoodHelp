package com.foodhelp.api.exception

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AccesoDenegadoException::class)
    fun accesoDenegado(e: AccesoDenegadoException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.FORBIDDEN, e.message ?: "Acceso Denegado")

    @ExceptionHandler(
        ArticuloNotFoundException::class,
        HogarNotFoundException::class,
        AlertaNotFoundException::class,
        MiembroNotFoundException::class,
    )
    fun noEncontrado(e: RuntimeException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "Recurso no encontrado")

    @ExceptionHandler(
        CantidadInsuficienteException::class,
        CantidadInvalidaException::class,
        SolicitudInvalidaException::class,
    )
    fun cantidadInvalida(e: RuntimeException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.BAD_REQUEST, e.message ?: "Operación inválida")

    @ExceptionHandler(
        UsuarioYaExisteException::class,
        UsuarioYaEnHogarException::class,
        UsuarioSinHogarException::class,
    )
    fun conflicto(e: RuntimeException): ResponseEntity<Map<String, Any>> = respuesta(HttpStatus.CONFLICT, e.message ?: "Conflicto")

    @ExceptionHandler(
        PessimisticLockingFailureException::class,
        CannotAcquireLockException::class,
    )
    fun bloqueoConcurrente(e: RuntimeException): ResponseEntity<Map<String, Any>> {
        log.warn("Conflicto de bloqueo concurrente: {}", e.message)
        return respuesta(HttpStatus.CONFLICT, "Operación en conflicto, inténtalo de nuevo")
    }

    @ExceptionHandler(CredencialesInvalidasException::class)
    fun credencialesInvalidas(e: CredencialesInvalidasException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.UNAUTHORIZED, e.message ?: "Credenciales inválidas")

    @ExceptionHandler(ContrasenaIncorrectaException::class)
    fun contrasenaIncorrecta(e: ContrasenaIncorrectaException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.FORBIDDEN, e.message ?: "Contraseña incorrecta")

    @ExceptionHandler(DemasiadosIntentosException::class)
    fun demasiadosIntentos(e: DemasiadosIntentosException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.TOO_MANY_REQUESTS, e.message ?: "Demasiados intentos. Inténtalo de nuevo más tarde")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val campos = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "valor inválido") }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to "Datos de entrada inválidos", "campos" to campos))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun cuerpoIlegible(e: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> =
        respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o está incompleto")

    // Las restricciones UNIQUE pueden colisionar por concurrencia (correo, código de hogar, alerta por artículo)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integridad(e: DataIntegrityViolationException): ResponseEntity<Map<String, Any>> {
        log.warn("Violación de integridad de datos: {}", e.mostSpecificCause.message)
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes")
    }

    private fun respuesta(
        status: HttpStatus,
        mensaje: String,
    ): ResponseEntity<Map<String, Any>> = ResponseEntity.status(status).body(mapOf("error" to mensaje))
}
