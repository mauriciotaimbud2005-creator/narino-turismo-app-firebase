package com.narino.turismo.domain.repository

import com.narino.turismo.domain.model.Place

interface PlacesRepository {
    suspend fun getPlaces(): List<Place>
    suspend fun toggleFavorite(placeId: String, currentStatus: Boolean): Boolean
    fun getCurrentUserEmail(): String
}
