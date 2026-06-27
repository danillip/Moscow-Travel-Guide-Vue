package com.example.routes

import com.example.model.RefreshTokenRequest
import com.example.model.SignInRequest
import com.example.model.SignOutRequest
import com.example.model.SignUpRequest
import com.example.model.PlaceCategoriesResponse
import com.example.model.respondError
import com.example.plugins.AuthService
import com.example.plugins.PlaceService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAuth(authService: AuthService, placeService: PlaceService) {
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "MTG BACKEND")
        }
        route("/api/v1") {
            get("/places") {
                val category = call.queryParameters["category"]
                val search = call.queryParameters["search"]
                val page = call.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.queryParameters["limit"]?.toIntOrNull() ?: 50
                val response = placeService.getPlaces(category, search, page, limit)
                call.respond(HttpStatusCode.OK, response)
            }

            get("/place-categories") {
                val categories = placeService.getCategories()
                call.respond(HttpStatusCode.OK, PlaceCategoriesResponse(items = categories))
            }

            get("/places/{placeId}") {
                val placeId = call.parameters["placeId"]!!
                val place = placeService.getPlaceById(placeId)
                if (place == null) {
                    call.respondError(HttpStatusCode.NotFound, "NOT_FOUND", "Место не найдено")
                    return@get
                }
                call.respond(HttpStatusCode.OK, place)
            }

            post("/sign-up") {
                val request = call.receive<SignUpRequest>()
                val response = try {
                    authService.signUp(request.name, request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.Conflict,
                        "EMAIL_ALREADY_EXISTS",
                        e.message ?: "Email уже зарегистрирован"
                    )
                    return@post
                }
                call.respond(HttpStatusCode.Created, response)
            }

            post("/sign-in") {
                val request = call.receive<SignInRequest>()
                val response = try {
                    authService.signIn(request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.Unauthorized,
                        "UNAUTHORIZED",
                        e.message ?: "Неверные учётные данные"
                    )
                    return@post
                }
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val request = call.receive<RefreshTokenRequest>()
                val response = try {
                    authService.refresh(request.refreshToken)
                } catch (_: Exception) {
                    call.respondError(
                        HttpStatusCode.Unauthorized,
                        "UNAUTHORIZED",
                        "Некорректный refresh токен"
                    )
                    return@post
                }
                call.respond(HttpStatusCode.OK, response)
            }

            authenticate("auth-jwt") {
                get("/me") {
                    val principal =
                        call.principal<JWTPrincipal>().also { println("principal: $it") }!!
                    val userId = principal.payload.subject
                    val response = try {
                        authService.getCurrentUser(userId)
                    } catch (_: IllegalArgumentException) {
                        call.respondError(
                            HttpStatusCode.NotFound,
                            "NOT_FOUND",
                            "Пользователь не найден"
                        )
                        return@get
                    }
                    call.respond(HttpStatusCode.OK, response)
                }

                post("/sign-out") {
                    val request = call.receive<SignOutRequest>()
                    try {
                        authService.signOut(request.refreshToken)
                    } catch (e: IllegalArgumentException) {
                        call.respondError(
                            HttpStatusCode.BadRequest,
                            "VALIDATION_ERROR",
                            e.message ?: "Некорректные данные"
                        )
                        return@post
                    }
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }
            }
        }
    }
}
