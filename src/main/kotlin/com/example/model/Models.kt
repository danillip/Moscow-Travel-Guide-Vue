package com.example.model

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDetail(
    val field: String? = null,
    val message: String
)

@Serializable
data class ErrorBody(
    val code: String,
    val message: String,
    val details: List<ErrorDetail>? = null
)

@Serializable
data class ApiErrorResponse(
    val error: ErrorBody
)

suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    code: String,
    message: String,
    details: List<ErrorDetail>? = null
) {
    respond(status, ApiErrorResponse(ErrorBody(code = code, message = message, details = details)))
}

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

@Serializable
data class PlaceCategoriesResponse(
    val items: List<String>
)
