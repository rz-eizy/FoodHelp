package com.foodhelp.api.exception

import com.foodhelp.api.dto.ErrorRespuesta
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AccesoDenegadoException::class)
    fun accesoDenegado(e: AccesoDenegadoException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.FORBIDDEN, e.message ?: "Acceso Denegado", e.codigo)

    @ExceptionHandler(ArticuloNotFoundException::class)
    fun articuloNoEncontrado(e: ArticuloNotFoundException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "Recurso no encontrado", e.codigo)

    @ExceptionHandler(HogarNotFoundException::class)
    fun hogarNoEncontrado(e: HogarNotFoundException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "Recurso no encontrado", e.codigo)

    @ExceptionHandler(AlertaNotFoundException::class)
    fun alertaNoEncontrada(e: AlertaNotFoundException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "Recurso no encontrado", e.codigo)

    @ExceptionHandler(MiembroNotFoundException::class)
    fun miembroNoEncontrado(e: MiembroNotFoundException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.NOT_FOUND, e.message ?: "Recurso no encontrado", e.codigo)

    @ExceptionHandler(CantidadInsuficienteException::class)
    fun cantidadInsuficiente(e: CantidadInsuficienteException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.BAD_REQUEST, e.message ?: "Operación inválida", e.codigo)

    @ExceptionHandler(CantidadInvalidaException::class)
    fun cantidadInvalida(e: CantidadInvalidaException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.BAD_REQUEST, e.message ?: "Operación inválida", e.codigo)

    @ExceptionHandler(SolicitudInvalidaException::class)
    fun solicitudInvalida(e: SolicitudInvalidaException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.BAD_REQUEST, e.message ?: "Operación inválida", e.codigo)

    @ExceptionHandler(UsuarioYaExisteException::class)
    fun usuarioYaExiste(e: UsuarioYaExisteException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.CONFLICT, e.message ?: "Conflicto", e.codigo)

    @ExceptionHandler(UsuarioYaEnHogarException::class)
    fun usuarioYaEnHogar(e: UsuarioYaEnHogarException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.CONFLICT, e.message ?: "Conflicto", e.codigo)

    @ExceptionHandler(UsuarioSinHogarException::class)
    fun usuarioSinHogar(e: UsuarioSinHogarException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.CONFLICT, e.message ?: "Conflicto", e.codigo)

    @ExceptionHandler(
        PessimisticLockingFailureException::class,
        CannotAcquireLockException::class,
    )
    fun bloqueoConcurrente(e: RuntimeException): ResponseEntity<ErrorRespuesta> {
        log.warn("Conflicto de bloqueo concurrente: {}", e.message)
        return respuesta(HttpStatus.CONFLICT, "Operación en conflicto, inténtalo de nuevo", CodigoError.CONFLICTO)
    }

    @ExceptionHandler(CredencialesInvalidasException::class)
    fun credencialesInvalidas(e: CredencialesInvalidasException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.UNAUTHORIZED, e.message ?: "Credenciales inválidas", e.codigo)

    @ExceptionHandler(ContrasenaIncorrectaException::class)
    fun contrasenaIncorrecta(e: ContrasenaIncorrectaException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.FORBIDDEN, e.message ?: "Contraseña incorrecta", e.codigo)

    @ExceptionHandler(DemasiadosIntentosException::class)
    fun demasiadosIntentos(e: DemasiadosIntentosException): ResponseEntity<ErrorRespuesta> =
        respuesta(
            HttpStatus.TOO_MANY_REQUESTS,
            e.message ?: "Demasiados intentos. Inténtalo de nuevo más tarde",
            e.codigo,
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(e: MethodArgumentNotValidException): ResponseEntity<ErrorRespuesta> {
        val campos = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "valor inválido") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorRespuesta("Datos de entrada inválidos", CodigoError.VALIDACION.name, campos),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun cuerpoIlegible(e: HttpMessageNotReadableException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o está incompleto", CodigoError.JSON_MALFORMADO)

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
    )
    fun parametroInvalido(e: RuntimeException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido", CodigoError.PARAMETRO_INVALIDO)

    @ExceptionHandler(NoResourceFoundException::class)
    fun rutaNoEncontrada(e: NoResourceFoundException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada", CodigoError.RUTA_NO_ENCONTRADA)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun metodoNoPermitido(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", CodigoError.METODO_NO_PERMITIDO)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun tipoContenidoNoSoportado(e: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado", CodigoError.TIPO_CONTENIDO_NO_SOPORTADO)

    @ExceptionHandler(AccessDeniedException::class)
    fun accesoDenegadoSpring(e: AccessDeniedException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.FORBIDDEN, "Acceso denegado", CodigoError.ACCESO_DENEGADO)

    @ExceptionHandler(AuthenticationException::class)
    fun noAutenticado(e: AuthenticationException): ResponseEntity<ErrorRespuesta> =
        respuesta(HttpStatus.UNAUTHORIZED, "No autenticado", CodigoError.NO_AUTENTICADO)

    // Las restricciones UNIQUE pueden colisionar por concurrencia (correo, código de hogar, alerta por artículo)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integridad(e: DataIntegrityViolationException): ResponseEntity<ErrorRespuesta> {
        log.warn("Violación de integridad de datos: {}", e.mostSpecificCause.message)
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", CodigoError.CONFLICTO)
    }

    @ExceptionHandler(Exception::class)
    fun errorInterno(e: Exception): ResponseEntity<ErrorRespuesta> {
        log.error("Error no controlado al atender la petición", e)
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", CodigoError.ERROR_INTERNO)
    }

    private fun respuesta(
        status: HttpStatus,
        mensaje: String,
        codigo: CodigoError,
    ): ResponseEntity<ErrorRespuesta> = ResponseEntity.status(status).body(ErrorRespuesta(mensaje, codigo.name))
}
