package com.foodhelp.api.security

import com.foodhelp.api.dto.ErrorRespuesta
import com.foodhelp.api.exception.CodigoError
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class RestAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json;charset=UTF-8"
        objectMapper.writeValue(
            response.outputStream,
            ErrorRespuesta("Acceso denegado", CodigoError.ACCESO_DENEGADO.name),
        )
    }
}
