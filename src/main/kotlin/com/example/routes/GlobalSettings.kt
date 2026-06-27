package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import kotlinx.serialization.json.Json

internal fun Application.globalSettings(jwtSecret: String, jwtIssuer: String, jwtAudience: String) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("type").isMissing) {
                    io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respondError(
                    HttpStatusCode.Unauthorized,
                    "UNAUTHORIZED",
                    "Некорректный или отсутствующий токен"
                )
            }
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respondError(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                "Некорректные данные запроса"
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respondError(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                cause.message ?: "Некорректные данные запроса"
            )
        }
        exception<Throwable> { call, _ ->
            call.respondError(
                HttpStatusCode.InternalServerError,
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера"
            )
        }
    }
}
