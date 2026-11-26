package com.foodhelp.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ClockConfig {
    @Bean
    fun clock(
        @Value("\${app.zona-horaria:America/Santiago}") zona: String,
    ): Clock = Clock.system(ZoneId.of(zona))
}
