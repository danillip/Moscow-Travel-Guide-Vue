package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun Application.globalSettings(jwtSecret: String, jwtIssuer: String, jwtAudience: String) {
    val log = LoggerFactory.getLogger("AuthMiddleware")

    log.info("JWT config: issuer=$jwtIssuer, audience=$jwtAudience, secret_len=${jwtSecret.length}")

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                log.info(
                    "JWT validate: subject=${credential.payload.subject}, issuer=${credential.payload.issuer}, audience=${credential.payload.audience}, type_claim=${
                        credential.payload.getClaim(
                            "type"
                        )
                    }, type_isMissing=${credential.payload.getClaim("type").isMissing}"
                )
                if (credential.payload.getClaim("type").isMissing) {
                    io.ktor.server.auth.UserIdPrincipal(credential.payload.subject)
                } else {
                    log.warn("JWT rejected: token has 'type' claim (refresh token?)")
                    null
                }
            }
            challenge { _, _ ->
                log.warn("JWT challenge triggered - token rejected")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or missing token")
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
        exception<Throwable> { call, cause ->
            log.error(
                "StatusPages caught exception: ${cause.javaClass.simpleName}: ${cause.message}",
                cause
            )
            val status = when (cause) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }
}
