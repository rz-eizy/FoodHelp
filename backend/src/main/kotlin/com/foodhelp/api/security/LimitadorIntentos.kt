package com.foodhelp.api.security

import com.foodhelp.api.exception.DemasiadosIntentosException
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class LimitadorIntentos(
    private val maxIntentos: Int,
    ventana: Duration,
    tamanoMaximo: Long = 10_000L,
) {
    private val cache: Cache<String, AtomicInteger> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(ventana)
            .maximumSize(tamanoMaximo)
            .build()

    fun exigirDisponible(clave: String) {
        val contador = cache.getIfPresent(clave)?.get() ?: 0
        if (contador >= maxIntentos) {
            throw DemasiadosIntentosException()
        }
    }

    fun registrarFallo(clave: String) {
        cache
            .get(clave) { AtomicInteger(0) }
            .incrementAndGet()
    }

    fun reiniciar(clave: String) {
        cache.invalidate(clave)
    }

    fun limpiarTodo() {
        cache.invalidateAll()
    }
}
