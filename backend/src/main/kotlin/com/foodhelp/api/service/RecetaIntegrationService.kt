package com.foodhelp.api.service

import com.foodhelp.api.dto.DetalleRecetaExternaDTO
import com.foodhelp.api.dto.IngredienteRecetaDTO
import com.foodhelp.api.dto.PasoRecetaDTO
import com.foodhelp.api.dto.RecetaDetalleDTO
import com.foodhelp.api.dto.RecetaExternaDTO
import com.foodhelp.api.exception.ServicioExternoNoDisponibleException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Service
class RecetaIntegrationService(
    restClientBuilder: RestClient.Builder,
    @Value("\${spoonacular.api.key}") apiKey: String,
    @Value("\${spoonacular.api.base-url:https://api.spoonacular.com}") baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(RecetaIntegrationService::class.java)

    // La clave viaja en cabecera: no aparece en la URL ni en los mensajes de excepción
    private val restClient =
        restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("x-api-key", apiKey)
            .build()

    @Cacheable(cacheNames = ["recetas"], key = "#p0.toString()", unless = "#result.isEmpty()")
    fun obtenerRecetasPorIngredientes(ingredientes: List<String>): List<RecetaExternaDTO> {
        if (ingredientes.isEmpty()) return emptyList()

        return try {
            restClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/recipes/findByIngredients")
                        .queryParam("ingredients", ingredientes.joinToString(","))
                        .queryParam("number", MAX_RECETAS)
                        .build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(object : ParameterizedTypeReference<List<RecetaExternaDTO>>() {})
                ?: emptyList()
        } catch (ex: RestClientResponseException) {
            // Se registra solo el estado HTTP, nunca la URL
            log.error("Spoonacular respondió con HTTP {}", ex.statusCode.value())
            emptyList()
        } catch (ex: RestClientException) {
            // Timeouts, DNS, conexión rechazada, JSON ilegible...
            log.error("Fallo al consultar Spoonacular: {}", ex.javaClass.simpleName)
            emptyList()
        }
    }

    @Cacheable(cacheNames = ["detalle-recetas"], key = "#p0", unless = "#result == null")
    fun obtenerDetalleReceta(id: Int): RecetaDetalleDTO? =
        try {
            val externa =
                restClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path("/recipes/{id}/information")
                            .queryParam("includeNutrition", false)
                            .build(id)
                    }.accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(DetalleRecetaExternaDTO::class.java)
            externa?.let { aDetalle(it) }
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) {
                log.info("Spoonacular no tiene la receta {}", id)
                null
            } else {
                log.error("Spoonacular respondió con HTTP {}", ex.statusCode.value())
                throw ServicioExternoNoDisponibleException()
            }
        } catch (ex: RestClientException) {
            log.error("Fallo al consultar Spoonacular: {}", ex.javaClass.simpleName)
            throw ServicioExternoNoDisponibleException()
        }

    private fun aDetalle(externa: DetalleRecetaExternaDTO) =
        RecetaDetalleDTO(
            id = externa.id,
            title = externa.title,
            image = externa.image,
            readyInMinutes = externa.readyInMinutes,
            servings = externa.servings,
            ingredients =
                externa.extendedIngredients.map {
                    IngredienteRecetaDTO(it.name, it.amount, it.unit, it.original)
                },
            steps =
                externa.analyzedInstructions
                    .flatMap { it.steps }
                    .mapIndexed { index, paso -> PasoRecetaDTO(index + 1, paso.step ?: "") },
        )

    private companion object {
        const val MAX_RECETAS = 10
    }
}
