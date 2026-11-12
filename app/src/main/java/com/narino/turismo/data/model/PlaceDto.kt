package com.narino.turismo.data.model

import com.narino.turismo.domain.model.Place

data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val municipality: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val isFavorite: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toDomain(isFavoriteOverride: Boolean = this.isFavorite): Place {
        return Place(
            id = id,
            name = name,
            municipality = municipality,
            category = category,
            description = description,
            imageUrl = imageUrl,
            rating = rating,
            isFavorite = isFavoriteOverride,
            latitude = latitude,
            longitude = longitude
        )
    }
}
