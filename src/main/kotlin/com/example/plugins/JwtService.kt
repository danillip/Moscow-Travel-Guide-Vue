package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class JwtConfig(val issuer: String, val audience: String, val secret: String)

@Singleton
class JwtService @Inject constructor(private val jwtConfig: JwtConfig) {
    private val algorithm: Algorithm = Algorithm.HMAC256(jwtConfig.secret)

    fun makeAccessToken(userId: String, email: String): String {
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
            .sign(algorithm)
    }

    fun makeRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .build()

    companion object {
        private const val ACCESS_TOKEN_EXPIRY = 3600000L
        const val REFRESH_TOKEN_EXPIRY = 604800000L
    }
}
