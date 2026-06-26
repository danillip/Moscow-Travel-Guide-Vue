package com.example.routes

import com.example.model.RefreshTokenRequest
import com.example.model.SignInRequest
import com.example.model.SignOutRequest
import com.example.model.SignUpRequest
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

            get("/places/{placeId}") {
                val placeId = call.parameters["placeId"]!!
                val place = placeService.getPlaceById(placeId)
                if (place == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Place not found"))
                    return@get
                }
                call.respond(HttpStatusCode.OK, place)
            }

            post("/sign-up") {
                val request = call.receive<SignUpRequest>()
                val response = try {
                    authService.signUp(request.name, request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                    return@post
                }
                call.respond(HttpStatusCode.Created, response)
            }

            post("/sign-in") {
                val request = call.receive<SignInRequest>()
                val response = try {
                    authService.signIn(request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
                    return@post
                }
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val request = call.receive<RefreshTokenRequest>()
                val response = try {
                    authService.refresh(request.refreshToken)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid refresh token")
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
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                        return@get
                    }
                    call.respond(HttpStatusCode.OK, response)
                }

                post("/sign-out") {
                    val request = call.receive<SignOutRequest>()
                    try {
                        authService.signOut(request.refreshToken)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        return@post
                    }
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }
            }
        }
    }
}
