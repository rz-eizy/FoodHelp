package com.foodhelp.api.service

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.InetSocketAddress
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RecetaIntegrationTimeoutTest {
    companion object {
        private val servidorColgado: HttpServer =
            HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
                createContext("/") { exchange ->
                    Thread.sleep(10_000) // simula un proveedor que no responde para comprobar el timeout
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun propiedades(registry: DynamicPropertyRegistry) {
            registry.add("spoonacular.api.base-url") { "http://localhost:${servidorColgado.address.port}" }
        }

        @JvmStatic
        @AfterAll
        fun detener() {
            servidorColgado.stop(0)
        }
    }

    @Autowired
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @Test
    fun `un Spoonacular colgado no bloquea mas alla del read-timeout`() {
        val inicio = System.nanoTime()

        val resultado = recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("tomato"))

        val segundos = Duration.ofNanos(System.nanoTime() - inicio).toMillis() / 1000.0
        assertTrue(resultado.isEmpty())
        assertTrue(segundos < 5.0, "El servicio tardó $segundos s: los timeouts HTTP no se están aplicando")
    }
}
