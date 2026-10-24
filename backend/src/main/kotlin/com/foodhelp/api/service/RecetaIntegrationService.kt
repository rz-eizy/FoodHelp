package com.foodhelp.api.service

import com.foodhelp.api.dto.RecetaExternaDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class RecetaIntegrationService(
    restClientBuilder: RestClient.Builder,
    @Value("\${spoonacular.api.key:test-api-key}")
    private val apiKey: String,
    @Value("\${spoonacular.api.base-url:https://api.spoonacular.com}")
    private val baseUrl: String = "https://api.spoonacular.com",
) {
    private val log = LoggerFactory.getLogger(RecetaIntegrationService::class.java)
    private val restClient = restClientBuilder.baseUrl(baseUrl).build()

    fun obtenerRecetasPorIngredientes(ingredientes: List<String>): List<RecetaExternaDTO> {
        if (ingredientes.isEmpty()) {
            return emptyList()
        }

        return try {
            val lista =
                restClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path("/recipes/findByIngredients")
                            .queryParam("ingredients", ingredientes.joinToString(","))
                            .queryParam("apiKey", apiKey)
                            .build()
                    }.accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(object : ParameterizedTypeReference<List<RecetaExternaDTO>>() {})

            lista ?: emptyList()
        } catch (ex: RestClientResponseException) {
            log.error("Error de respuesta HTTP al consultar Spoonacular (${ex.statusCode}): ${ex.message}")
            emptyList()
        } catch (ex: Exception) {
            log.error("Excepción al consultar API de recetas Spoonacular: ${ex.message}", ex)
            emptyList()
        }
    }
}
