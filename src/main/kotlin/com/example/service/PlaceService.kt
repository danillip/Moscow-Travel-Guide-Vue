package com.example.service

import com.example.model.Place
import com.example.model.PlacesResponse

internal interface PlaceService {
    fun getPlaces(category: String?, search: String?, page: Int, limit: Int): PlacesResponse
    fun getPlaceById(id: String): Place?
    fun getCategories(): List<String>
}
