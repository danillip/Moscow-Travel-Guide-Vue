package com.example.di

import com.example.plugins.AuthService
import com.example.plugins.DatabaseFactory
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, JwtModule::class])
interface AppComponent {
    fun authService(): AuthService
    fun databaseFactory(): DatabaseFactory
}
