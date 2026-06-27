package com.example.di

import com.example.database.DatabaseFactory
import com.example.database.DbConfig
import dagger.Module
import dagger.Provides
import io.ktor.server.config.ApplicationConfig
import javax.inject.Singleton

@Module
internal class DatabaseModule {

    @Provides
    fun provideDbConfig(config: ApplicationConfig): DbConfig = DbConfig(
        url = config.property("db.url").getString(),
        user = config.property("db.user").getString(),
        password = config.property("db.password").getString()
    )

    @Provides
    fun provideDatabaseFactory(dbConfig: DbConfig): DatabaseFactory = DatabaseFactory(dbConfig)
}
