package com.foodhelp.api.service

import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.Hogar
import com.foodhelp.api.repository.ArticuloRepository
import com.foodhelp.api.repository.HogarRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InventarioConcurrenciaTest {
    @Autowired
    private lateinit var inventarioService: InventarioService

    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @AfterEach
    fun limpiar() {
        articuloRepository.deleteAll()
        hogarRepository.deleteAll()
    }

    @Test
    fun `40 restas concurrentes de 1 sobre stock 20 tienen exactamente 20 exitos y eliminan el articulo`() {
        val hogar = hogarRepository.save(Hogar(nombre = "Concurrencia", codigoHogar = "CONC-TEST"))
        val articulo =
            articuloRepository.save(
                ArticuloDespensa(
                    nombre = "Leche",
                    fechaVencimiento = LocalDate.now().plusDays(5),
                    cantidad = 20,
                    hogar = hogar,
                ),
            )

        val intentos = 40
        val pool = Executors.newFixedThreadPool(8)
        val salida = CountDownLatch(1)

        val resultados =
            (1..intentos).map {
                pool.submit(
                    Callable {
                        salida.await()
                        runCatching { inventarioService.restarCantidad(articulo.id!!, hogar.id!!, 1) }.isSuccess
                    },
                )
            }
        salida.countDown()
        val exitos = resultados.count { it.get() }
        pool.shutdown()

        assertEquals(20, exitos)
        assertFalse(articuloRepository.existsById(articulo.id!!))
    }
}
