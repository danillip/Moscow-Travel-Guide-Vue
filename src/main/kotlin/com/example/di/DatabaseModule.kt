package com.example.di

import com.example.plugins.DatabaseFactory
import com.example.plugins.DbConfig
import dagger.Module
import dagger.Provides
import io.ktor.server.config.ApplicationConfig
import javax.inject.Singleton

@Module
class DatabaseModule(private val config: ApplicationConfig) {

    @Provides
    fun provideDbConfig(): DbConfig = DbConfig(
        url = config.property("db.url").getString(),
        user = config.property("db.user").getString(),
        password = config.property("db.password").getString()
    )

    @Provides
    @Singleton
    fun provideDatabaseFactory(dbConfig: DbConfig): DatabaseFactory = DatabaseFactory(dbConfig)
}
