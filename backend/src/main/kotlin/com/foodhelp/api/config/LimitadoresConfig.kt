package com.foodhelp.api.config

import com.foodhelp.api.security.LimitadorIntentos
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class LimitadoresConfig {
    @Bean("limitadorLogin")
    fun limitadorLogin(
        @Value("\${app.limites.login.max-intentos:10}") maxIntentos: Int,
        @Value("\${app.limites.login.ventana-minutos:5}") ventanaMinutos: Long,
    ): LimitadorIntentos =
        LimitadorIntentos(
            maxIntentos = maxIntentos,
            ventana = Duration.ofMinutes(ventanaMinutos),
        )

    @Bean("limitadorUnirse")
    fun limitadorUnirse(
        @Value("\${app.limites.unirse.max-intentos:10}") maxIntentos: Int,
        @Value("\${app.limites.unirse.ventana-minutos:10}") ventanaMinutos: Long,
    ): LimitadorIntentos =
        LimitadorIntentos(
            maxIntentos = maxIntentos,
            ventana = Duration.ofMinutes(ventanaMinutos),
        )
}
