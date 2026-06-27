package com.example

import com.example.di.AppComponent
import com.example.di.DaggerAppComponent
import com.example.routes.configureAuth
import com.example.routes.configureDefault
import com.example.routes.configureNoAuth
import com.example.routes.globalSettings
import com.example.service.AuthService
import com.example.service.PlaceService
import com.example.service.RouteService
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

internal fun Application.module() {
    val component: AppComponent = DaggerAppComponent.factory()
        .create(environment.config).also { it.databaseFactory().init() }

    val authService: AuthService = component.authService()
    val routeService: RouteService = component.routeService()
    val placeService: PlaceService = component.placeService()

    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    globalSettings(jwtSecret, jwtIssuer, jwtAudience)
    configureDefault()
    configureAuth(authService = authService, routeService = routeService)
    configureNoAuth(authService = authService, placeService = placeService)
}
