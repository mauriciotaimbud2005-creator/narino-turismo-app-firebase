package com.narino.turismo.domain.model

data class Place(
    val id: String,
    val name: String,
    val municipality: String,
    val category: String,
    val imageUrl: String,
    val rating: Double,
    val description: String,
    val isFavorite: Boolean = false
)
