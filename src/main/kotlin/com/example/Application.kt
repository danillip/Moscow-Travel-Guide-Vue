package com.example

import com.example.di.DaggerAppComponent
import com.example.di.DatabaseModule
import com.example.di.JwtModule
import com.example.routes.configureAuth
import com.example.routes.globalSettings
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val component = DaggerAppComponent.builder()
        .databaseModule(DatabaseModule(environment.config))
        .jwtModule(JwtModule(environment.config))
        .build()

    component.databaseFactory().init()

    globalSettings()
    configureAuth(component.authService())
}
