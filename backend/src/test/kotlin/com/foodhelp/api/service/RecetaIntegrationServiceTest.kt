package com.foodhelp.api.service

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(RecetaIntegrationService::class)
@TestPropertySource(
    properties = [
        "spoonacular.api.key=api_key_test_123",
        "spoonacular.api.base-url=https://api.spoonacular.com",
    ],
)
class RecetaIntegrationServiceTest {
    @Autowired
    private lateinit var recetaIntegrationService: RecetaIntegrationService

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `Test 1 - simula JSON valido de Spoonacular y deserializa correctamente a RecetaExternaDTO`() {
        val jsonMock =
            """
            [
              {
                "id": 716429,
                "title": "Pasta con Tomate y Albahaca",
                "image": "https://spoonacular.com/recipeImages/716429-312x231.jpg",
                "missedIngredientCount": 1
              }
            ]
            """.trimIndent()

        server
            .expect(requestTo(containsString("/recipes/findByIngredients")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(jsonMock, MediaType.APPLICATION_JSON))

        val resultado = recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("tomate", "albahaca"))

        assertEquals(1, resultado.size)
        val receta = resultado[0]
        assertEquals(716429, receta.id)
        assertEquals("Pasta con Tomate y Albahaca", receta.title)
        assertEquals("https://spoonacular.com/recipeImages/716429-312x231.jpg", receta.image)
        assertEquals(1, receta.missedIngredientCount)
        server.verify()
    }

    @Test
    fun `Test 2 - simula respuesta HTTP 500 y comprueba tolerancia a fallos retornando lista vacia`() {
        server
            .expect(requestTo(containsString("/recipes/findByIngredients")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        val resultado = recetaIntegrationService.obtenerRecetasPorIngredientes(listOf("carne", "papas"))

        assertTrue(resultado.isEmpty(), "Ante error 500 del servidor externo debe retornar una lista vacía")
        server.verify()
    }

    @Test
    fun `Test 3 - verifica que la URL generada contenga los ingredientes delimitados por comas y la API Key`() {
        val ingredientes = listOf("huevo", "leche", "harina")
        val expectedIngredientsParam = "huevo,leche,harina"
        val expectedApiKey = "api_key_test_123"

        server
            .expect(requestTo(containsString("ingredients=$expectedIngredientsParam")))
            .andExpect(requestTo(containsString("apiKey=$expectedApiKey")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON))

        val resultado = recetaIntegrationService.obtenerRecetasPorIngredientes(ingredientes)

        assertTrue(resultado.isEmpty())
        server.verify()
    }
}
