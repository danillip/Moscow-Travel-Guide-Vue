package com.example.service

import com.auth0.jwt.JWTVerifier

internal interface JwtService {
    fun makeAccessToken(userId: String, email: String): String
    fun makeRefreshToken(userId: String): String
    fun verifier(): JWTVerifier
}
