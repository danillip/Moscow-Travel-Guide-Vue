package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JWTConfig {
    private lateinit var algorithm: Algorithm
    lateinit var issuer: String
    private lateinit var audience: String
    private const val ACCESS_TOKEN_EXPIRY = 3600000L
    private const val REFRESH_TOKEN_EXPIRY = 604800000L

    fun init(issuer: String, audience: String, secret: String) {
        this.issuer = issuer
        this.audience = audience
        this.algorithm = Algorithm.HMAC256(secret)
    }

    fun makeAccessToken(userId: String, email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
            .sign(algorithm)
    }

    fun makeRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}
