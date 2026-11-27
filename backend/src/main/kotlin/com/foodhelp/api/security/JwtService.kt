package com.foodhelp.api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration:86400000}") private val jwtExpiration: Long,
) {
    private val signingKey: SecretKey =
        secret.toByteArray(StandardCharsets.UTF_8).let { bytes ->
            require(bytes.size >= MIN_SECRET_BYTES) {
                "jwt.secret debe tener al menos $MIN_SECRET_BYTES bytes (256 bits)."
            }
            Keys.hmacShaKeyFor(bytes)
        }

    @JvmOverloads
    fun generateToken(
        username: String,
        extraClaims: Map<String, Any> = emptyMap(),
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + jwtExpiration))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUsername(token: String): String? = extractAllClaims(token)?.subject

    fun extractTokenVersion(token: String): Int? = extractAllClaims(token)?.get("tv", Int::class.javaObjectType)

    fun <T> extractClaim(
        token: String,
        claimsResolver: (Claims) -> T,
    ): T? = extractAllClaims(token)?.let(claimsResolver)

    fun extractAllClaims(token: String): Claims? =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    fun isTokenValid(
        token: String,
        username: String,
    ): Boolean {
        val claims = extractAllClaims(token) ?: return false
        return claims.subject == username && claims.expiration?.after(Date()) == true
    }

    fun isTokenExpired(token: String): Boolean = extractAllClaims(token)?.expiration?.before(Date()) ?: true

    private companion object {
        const val MIN_SECRET_BYTES = 32
    }
}
