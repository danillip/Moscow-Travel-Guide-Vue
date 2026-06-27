package com.example.di

import com.example.service.AuthService
import com.example.service.AuthServiceImpl
import com.example.service.JwtService
import com.example.service.JwtServiceImpl
import com.example.service.PlaceService
import com.example.service.PlaceServiceImpl
import com.example.service.RouteService
import com.example.service.RouteServiceImpl
import dagger.Binds
import dagger.Module

@Module
internal interface ServiceModule {

    @Binds
    fun bindAuthService(impl: AuthServiceImpl): AuthService

    @Binds
    fun bindPlaceService(impl: PlaceServiceImpl): PlaceService

    @Binds
    fun bindRouteService(impl: RouteServiceImpl): RouteService

    @Binds
    fun bindJwtService(impl: JwtServiceImpl): JwtService
}
