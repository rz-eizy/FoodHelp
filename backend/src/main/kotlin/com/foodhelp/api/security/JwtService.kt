package com.foodhelp.api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private val secretKey: String = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    @Value("\${jwt.expiration:86400000}")
    private val jwtExpiration: Long = 86400000L,
) {
    @JvmOverloads
    fun generateToken(
        username: String,
        extraClaims: Map<String, Any> = emptyMap(),
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUsername(token: String): String? = extractClaim(token, Claims::getSubject)

    fun <T> extractClaim(
        token: String,
        claimsResolver: (Claims) -> T,
    ): T? {
        val claims = extractAllClaims(token) ?: return null
        return claimsResolver(claims)
    }

    fun extractAllClaims(token: String): Claims? =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            null
        }

    fun isTokenValid(
        token: String,
        username: String,
    ): Boolean {
        val extractedUsername = extractUsername(token) ?: return false
        return extractedUsername == username && !isTokenExpired(token)
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token, Claims::getExpiration) ?: return true
        return expiration.before(Date())
    }

    private fun getSigningKey(): Key {
        val keyBytes = secretKey.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
