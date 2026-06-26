package com.example.plugins

import com.example.model.Place
import com.example.model.PlacesMeta
import com.example.model.PlacesResponse
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceService @Inject constructor() {

    private val places: List<Place> by lazy {
        val json = this::class.java.classLoader.getResourceAsStream("places.json")!!
            .bufferedReader().use { it.readText() }
        Json.decodeFromString<List<Place>>(json)
    }

    fun getPlaces(
        category: String?,
        search: String?,
        page: Int,
        limit: Int
    ): PlacesResponse {
        var filtered = places.filter { it.isActive }

        if (!category.isNullOrBlank()) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (!search.isNullOrBlank()) {
            val query = search.lowercase()
            filtered = filtered.filter { place ->
                place.title.lowercase().contains(query) ||
                        place.description.lowercase().contains(query) ||
                        place.category.lowercase().contains(query)
            }
        }

        val total = filtered.size
        val offset = (page - 1) * limit
        val items = filtered.drop(offset).take(limit)

        return PlacesResponse(
            items = items,
            meta = PlacesMeta(
                page = page,
                limit = limit,
                total = total,
                hasMore = page * limit < total
            )
        )
    }

    fun getPlaceById(id: String): Place? {
        return places.find { it.id == id && it.isActive }
    }
}
