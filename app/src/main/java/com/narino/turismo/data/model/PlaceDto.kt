package com.narino.turismo.data.model

import com.narino.turismo.domain.model.Place

data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val municipality: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val description: String = "",
    val isFavorite: Boolean = false
) {
    fun toDomain(isFavoriteOverride: Boolean = this.isFavorite): Place {
        return Place(
            id = id,
            name = name,
            municipality = municipality,
            category = category,
            imageUrl = imageUrl,
            rating = rating,
            description = description,
            isFavorite = isFavoriteOverride
        )
    }
}
