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

@Serializable
data class SignOutRequest(
    val refreshToken: String
)

@Serializable
data class CurrentUserResponse(
    val user: UserResponse
)

@Serializable
data class Place(
    val id: String,
    val title: String,
    val category: String,
    val coordinates: List<Double>,
    val visitMinutes: Int,
    val ticketStatus: String,
    val description: String,
    val highlight: String,
    val isActive: Boolean = true
)

@Serializable
data class PlacesResponse(
    val items: List<Place>,
    val meta: PlacesMeta
)

@Serializable
data class PlacesMeta(
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasMore: Boolean
)
