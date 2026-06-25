package com.example.routes

import com.example.model.RefreshTokenRequest
import com.example.model.SignInRequest
import com.example.model.SignOutRequest
import com.example.model.SignUpRequest
import com.example.plugins.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory


fun Application.configureAuth(authService: AuthService) {
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "MTG BACKEND")
        }
        route("/api/v1") {
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
                post("/sign-out") {
                    val principal = call.principal<UserIdPrincipal>()
                    log.info("Sign-out request from user=${principal?.name}")
                    val authHeader = call.request.headers["Authorization"]
                    log.info("Sign-out Authorization header: $authHeader")
                    val request = call.receive<SignOutRequest>()
                    log.info("Sign-out body: refreshToken_prefix=${request.refreshToken.take(20)}...")
                    authService.signOut(request.refreshToken)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }
            }
        }
    }
}
