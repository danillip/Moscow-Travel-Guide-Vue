package com.example.model

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorDetail(
    val field: String? = null,
    val message: String
)

@Serializable
internal data class ErrorBody(
    val code: String,
    val message: String,
    val details: List<ErrorDetail>? = null
)

@Serializable
internal data class ApiErrorResponse(
    val error: ErrorBody
)

internal suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    code: String,
    message: String,
    details: List<ErrorDetail>? = null
): Unit =
    respond(status, ApiErrorResponse(ErrorBody(code = code, message = message, details = details)))

@Serializable
internal data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
internal data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
internal data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
internal data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)

@Serializable
internal data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
internal data class SignOutRequest(
    val refreshToken: String
)

@Serializable
internal data class CurrentUserResponse(
    val user: UserResponse
)

@Serializable
internal data class Place(
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
internal data class PlacesResponse(
    val items: List<Place>,
    val meta: PlacesMeta
)

@Serializable
internal data class PlacesMeta(
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasMore: Boolean
)

@Serializable
internal data class PlaceCategoriesResponse(
    val items: List<String>
)

@Serializable
internal data class RouteInfo(
    val canVisit: Boolean,
    val distanceKm: Double,
    val totalSeconds: Int,
    val timeText: String,
    val distanceText: String,
    val message: String
)

@Serializable
internal data class SavedRoute(
    val id: String,
    val userId: String,
    val title: String,
    val type: String,
    val points: List<List<Double>>,
    val placeIds: List<String>,
    val freeTimeHours: Int,
    val humanSpeedKmH: Int,
    val routeInfo: RouteInfo,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class SavedRoutesResponse(
    val items: List<SavedRoute>,
    val meta: PlacesMeta
)

@Serializable
internal data class CreateRouteRequest(
    val title: String,
    val type: String,
    val points: List<List<Double>>,
    val placeIds: List<String>,
    val freeTimeHours: Int,
    val humanSpeedKmH: Int,
    val routeInfo: RouteInfo
)

@Serializable
internal data class UpdateRouteRequest(
    val title: String? = null,
    val type: String? = null,
    val points: List<List<Double>>? = null,
    val placeIds: List<String>? = null,
    val freeTimeHours: Int? = null,
    val humanSpeedKmH: Int? = null,
    val routeInfo: RouteInfo? = null
)
