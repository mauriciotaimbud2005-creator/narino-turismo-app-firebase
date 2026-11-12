package com.narino.turismo.domain.model

data class Place(
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
)
