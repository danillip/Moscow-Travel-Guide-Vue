package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
