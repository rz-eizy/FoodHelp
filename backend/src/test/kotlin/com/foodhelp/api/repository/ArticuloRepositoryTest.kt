package com.foodhelp.api.repository

import com.foodhelp.api.models.ArticuloDespensa
import com.foodhelp.api.models.CategoriaEnum
import com.foodhelp.api.models.Hogar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
class ArticuloRepositoryTest {
    @Autowired
    private lateinit var articuloRepository: ArticuloRepository

    @Autowired
    private lateinit var hogarRepository: HogarRepository

    @Test
    fun `guardar articulo sin fechaVencimiento lanza DataIntegrityViolationException`() {
        val hogar = hogarRepository.saveAndFlush(Hogar(nombre = "Hogar Test", codigoHogar = "HOGAR-01"))
        val articuloSinFecha =
            ArticuloDespensa(
                nombre = "Yogurt",
                fechaVencimiento = null,
                cantidad = 2,
                categoria = CategoriaEnum.LACTEOS,
                hogar = hogar,
            )

        assertThrows<DataIntegrityViolationException> {
            articuloRepository.saveAndFlush(articuloSinFecha)
        }
    }

    @Test
    fun `al eliminar un Hogar sus articulos asociados se eliminan en cascada`() {
        val hogar = hogarRepository.saveAndFlush(Hogar(nombre = "Hogar Cascada", codigoHogar = "CASCADA-01"))
        val art1 =
            ArticuloDespensa(
                nombre = "Manzanas",
                fechaVencimiento = LocalDate.now().plusDays(7),
                cantidad = 6,
                categoria = CategoriaEnum.FRUTAS,
                hogar = hogar,
            )
        val art2 =
            ArticuloDespensa(
                nombre = "Pollo",
                fechaVencimiento = LocalDate.now().plusDays(3),
                cantidad = 1,
                categoria = CategoriaEnum.CARNES,
                hogar = hogar,
            )
        articuloRepository.saveAndFlush(art1)
        articuloRepository.saveAndFlush(art2)

        val articulosAntes = articuloRepository.findByHogarId(hogar.id!!)
        assertEquals(2, articulosAntes.size)

        // Eliminamos el hogar padre
        hogarRepository.delete(hogar)
        hogarRepository.flush()

        val articulosDespues = articuloRepository.findByHogarId(hogar.id!!)
        assertTrue(articulosDespues.isEmpty())
        assertFalse(articuloRepository.existsById(art1.id!!))
        assertFalse(articuloRepository.existsById(art2.id!!))
    }

    @Test
    fun `recuperar lista de articulos filtrados por hogar id`() {
        val hogar = hogarRepository.saveAndFlush(Hogar(nombre = "Hogar Filtrado", codigoHogar = "FILTRO-01"))
        val articulo =
            ArticuloDespensa(
                nombre = "Arroz",
                fechaVencimiento = LocalDate.now().plusMonths(6),
                cantidad = 5,
                categoria = CategoriaEnum.DESPENSA,
                hogar = hogar,
            )
        articuloRepository.saveAndFlush(articulo)

        val lista = articuloRepository.findByHogarId(hogar.id!!)
        assertEquals(1, lista.size)
        assertEquals("Arroz", lista[0].nombre)
    }
}
