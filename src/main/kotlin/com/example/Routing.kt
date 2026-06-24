package com.example

import com.example.model.RefreshTokenRequest
import com.example.model.SignInRequest
import com.example.model.SignUpRequest
import com.example.plugins.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "MTG BACKEND")
        }
        route("/api/v1") {
            post("/sign-up") {
                val request = call.receive<SignUpRequest>()
                val response = try {
                    AuthService.signUp(request.name, request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                    return@post
                }
                call.respond(HttpStatusCode.Created, response)
            }

            post("/sign-in") {
                val request = call.receive<SignInRequest>()
                val response = try {
                    AuthService.signIn(request.email, request.password)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
                    return@post
                }
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val request = call.receive<RefreshTokenRequest>()
                val response = try {
                    AuthService.refresh(request.refreshToken)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid refresh token")
                    )
                    return@post
                }
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
