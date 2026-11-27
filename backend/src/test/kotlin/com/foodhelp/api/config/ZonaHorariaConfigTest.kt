package com.foodhelp.api.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Clock

@SpringBootTest
class ZonaHorariaConfigTest(
    @Autowired private val clock: Clock,
) {
    @Test
    fun `el reloj usa America Santiago cuando APP_TIMEZONE no esta definida`() {
        assertEquals("America/Santiago", clock.zone.id)
    }
}
