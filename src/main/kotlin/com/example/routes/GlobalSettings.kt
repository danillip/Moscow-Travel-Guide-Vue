package com.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json

fun Application.globalSettings() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = when (cause) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }
}
