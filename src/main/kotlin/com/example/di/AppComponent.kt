package com.example.di

import com.example.plugins.AuthService
import com.example.plugins.DatabaseFactory
import com.example.plugins.PlaceService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, JwtModule::class])
interface AppComponent {
    fun authService(): AuthService
    fun databaseFactory(): DatabaseFactory
    fun placeService(): PlaceService
}
