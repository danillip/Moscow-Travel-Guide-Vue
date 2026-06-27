package com.example.service

import com.example.model.CreateRouteRequest
import com.example.model.SavedRoute
import com.example.model.SavedRoutesResponse
import com.example.model.UpdateRouteRequest

internal interface RouteService {
    fun getRoutes(userId: String, page: Int, limit: Int): SavedRoutesResponse
    fun getRouteById(userId: String, routeId: String): SavedRoute?
    fun createRoute(userId: String, request: CreateRouteRequest): SavedRoute
    fun updateRoute(userId: String, routeId: String, request: UpdateRouteRequest): SavedRoute?
    fun deleteRoute(userId: String, routeId: String): Boolean
}
