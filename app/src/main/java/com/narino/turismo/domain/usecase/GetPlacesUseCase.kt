package com.narino.turismo.domain.usecase

import com.narino.turismo.domain.model.Place
import com.narino.turismo.domain.repository.PlacesRepository

class GetPlacesUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(
        municipality: String = "Todos",
        category: String? = null,
        searchQuery: String = "",
        onlyFavorites: Boolean = false
    ): List<Place> {
        val allPlaces = repository.getPlaces()
        return allPlaces.filter { place ->
            val matchesMunicipality = municipality == "Todos" || place.municipality.equals(municipality, ignoreCase = true)
            val matchesCategory = category == null || category == "Todos" || place.category.equals(category, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                place.name.contains(searchQuery, ignoreCase = true) || 
                place.description.contains(searchQuery, ignoreCase = true) ||
                place.municipality.contains(searchQuery, ignoreCase = true)
            val matchesFavorites = !onlyFavorites || place.isFavorite

            matchesMunicipality && matchesCategory && matchesSearch && matchesFavorites
        }
    }
}
