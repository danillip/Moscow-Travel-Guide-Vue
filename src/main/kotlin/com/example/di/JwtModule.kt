package com.example.di

import com.example.service.JwtConfig
import dagger.Module
import dagger.Provides
import io.ktor.server.config.ApplicationConfig

@Module
internal class JwtModule {

    @Provides
    fun provideJwtConfig(config: ApplicationConfig): JwtConfig = JwtConfig(
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        secret = config.property("jwt.secret").getString()
    )
}
