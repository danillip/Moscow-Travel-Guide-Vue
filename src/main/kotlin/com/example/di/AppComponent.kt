package com.example.di

import com.example.service.AuthService
import com.example.database.DatabaseFactory
import com.example.service.PlaceService
import com.example.service.RouteService
import dagger.BindsInstance
import dagger.Component
import io.ktor.server.config.ApplicationConfig
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, JwtModule::class, ServiceModule::class])
internal interface AppComponent {
    fun authService(): AuthService
    fun databaseFactory(): DatabaseFactory
    fun placeService(): PlaceService
    fun routeService(): RouteService

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance config: ApplicationConfig): AppComponent
    }
}
