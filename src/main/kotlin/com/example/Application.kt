package com.example

import com.example.routes.configureAuth
import com.example.routes.globalSettings
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    globalSettings()
    configureAuth()
}
