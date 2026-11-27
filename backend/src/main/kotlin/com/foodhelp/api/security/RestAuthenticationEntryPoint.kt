package com.foodhelp.api.security

import com.foodhelp.api.dto.ErrorRespuesta
import com.foodhelp.api.exception.CodigoError
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        objectMapper.writeValue(
            response.outputStream,
            ErrorRespuesta("No autenticado", CodigoError.NO_AUTENTICADO.name),
        )
    }
}
