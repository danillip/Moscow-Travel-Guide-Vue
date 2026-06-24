package com.example.di

import com.example.plugins.JwtConfig
import com.example.plugins.JwtService
import dagger.Module
import dagger.Provides
import io.ktor.server.config.ApplicationConfig
import javax.inject.Singleton

@Module
class JwtModule(private val config: ApplicationConfig) {

    @Provides
    fun provideJwtConfig(): JwtConfig = JwtConfig(
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        secret = config.property("jwt.secret").getString()
    )

    @Provides
    @Singleton
    fun provideJwtService(jwtConfig: JwtConfig): JwtService = JwtService(jwtConfig)
}
