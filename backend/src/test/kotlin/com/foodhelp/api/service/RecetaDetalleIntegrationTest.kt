package com.foodhelp.api.service

import com.foodhelp.api.exception.ServicioExternoNoDisponibleException
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(RecetaIntegrationService::class)
@Import(RecetaDetalleIntegrationTest.CachePruebaConfig::class)
@TestPropertySource(
    properties = [
        "spoonacular.api.key=api_key_test_123",
        "spoonacular.api.base-url=https://api.spoonacular.com",
    ],
)
class RecetaDetalleIntegrationTest {
    @TestConfiguration
    @EnableCaching
    class CachePruebaConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager("recetas", "detalle-recetas")
    }

    @Autowired
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `mapea ingredientes y aplana pasos de varias secciones renumerandolos`() {
        val json =
            """
            {
              "id": 716429,
              "title": "Pasta con tomate",
              "image": "https://spoonacular.com/recipeImages/716429.jpg",
              "readyInMinutes": 25,
              "servings": 2,
              "extendedIngredients": [
                {"name": "tomate", "amount": 2.0, "unit": "unidades", "original": "2 tomates", "aisle": "verduras"},
                {"name": "albahaca", "amount": 1.0, "unit": "puñado", "original": "1 puñado de albahaca"}
              ],
              "analyzedInstructions": [
                {"name": "pasta", "steps": [{"number": 1, "step": "Cocer la pasta"}]},
                {"name": "salsa", "steps": [
                  {"number": 1, "step": "Añadir el tomate"},
                  {"number": 2, "step": "Espolvorear albahaca"}
                ]}
              ]
            }
            """.trimIndent()

        server
            .expect(requestTo(containsString("/recipes/716429/information")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(requestTo(containsString("includeNutrition=false")))
            .andExpect(header("x-api-key", "api_key_test_123"))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

        val detalle = recetaIntegrationService.obtenerDetalleReceta(716429)!!

        assertEquals(716429, detalle.id)
        assertEquals("Pasta con tomate", detalle.title)
        assertEquals(25, detalle.readyInMinutes)
        assertEquals(2, detalle.servings)
        assertEquals(2, detalle.ingredients.size)
        assertEquals("tomate", detalle.ingredients.first().name)
        assertEquals(2.0, detalle.ingredients.first().amount)
        assertEquals("2 tomates", detalle.ingredients.first().original)
        assertEquals(listOf(1, 2, 3), detalle.steps.map { it.number })
        assertEquals("Cocer la pasta", detalle.steps[0].step)
        assertEquals("Añadir el tomate", detalle.steps[1].step)
        assertEquals("Espolvorear albahaca", detalle.steps[2].step)
        server.verify()
    }

    @Test
    fun `sin instrucciones devuelve pasos vacios`() {
        val json =
            """
            {"id": 42, "title": "Tostada", "analyzedInstructions": []}
            """.trimIndent()

        server
            .expect(requestTo(containsString("/recipes/42/information")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

        val detalle = recetaIntegrationService.obtenerDetalleReceta(42)!!

        assertTrue(detalle.steps.isEmpty())
        assertTrue(detalle.ingredients.isEmpty())
        assertNull(detalle.image)
        assertNull(detalle.readyInMinutes)
        assertNull(detalle.servings)
        server.verify()
    }

    @Test
    fun `receta inexistente devuelve null sin lanzar error externo`() {
        server
            .expect(requestTo(containsString("/recipes/999/information")))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        assertNull(recetaIntegrationService.obtenerDetalleReceta(999))
        server.verify()
    }

    @Test
    fun `error del proveedor lanza servicio externo no disponible`() {
        server
            .expect(requestTo(containsString("/recipes/7/information")))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertThrows(ServicioExternoNoDisponibleException::class.java) {
            recetaIntegrationService.obtenerDetalleReceta(7)
        }
        server.verify()
    }

    @Test
    fun `la clave de api no aparece en la url`() {
        server
            .expect(requestTo(not(containsString("api_key_test_123"))))
            .andExpect(header("x-api-key", "api_key_test_123"))
            .andRespond(withSuccess("""{"id":1,"title":"Receta"}""", MediaType.APPLICATION_JSON))

        val detalle = recetaIntegrationService.obtenerDetalleReceta(1)!!

        assertEquals(1, detalle.id)
        server.verify()
    }

    @Test
    fun `la segunda consulta de una receta no vuelve a spoonacular`() {
        server
            .expect(requestTo(containsString("/recipes/55/information")))
            .andRespond(withSuccess("""{"id":55,"title":"Receta en cache"}""", MediaType.APPLICATION_JSON))

        val primera = recetaIntegrationService.obtenerDetalleReceta(55)
        val segunda = recetaIntegrationService.obtenerDetalleReceta(55)

        assertEquals(primera, segunda)
        assertEquals("Receta en cache", segunda!!.title)
        server.verify()
    }
}
