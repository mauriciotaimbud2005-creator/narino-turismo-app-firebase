package com.narino.turismo.domain.usecase

import com.narino.turismo.domain.model.Place
import com.narino.turismo.domain.repository.PlacesRepository
import java.text.Normalizer

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
        val normalizedQuery = normalizeText(searchQuery)
        val normalizedMuni = normalizeText(municipality)
        val normalizedCat = category?.let { normalizeText(it) }

        return allPlaces.filter { place ->
            val placeNameNorm = normalizeText(place.name)
            val placeDescNorm = normalizeText(place.description)
            val placeMuniNorm = normalizeText(place.municipality)
            val placeCatNorm = normalizeText(place.category)

            val matchesMunicipality = normalizedMuni == "todos" || placeMuniNorm == normalizedMuni
            val matchesCategory = normalizedCat == null || normalizedCat == "todos" || placeCatNorm == normalizedCat
            val matchesSearch = normalizedQuery.isBlank() ||
                    placeNameNorm.contains(normalizedQuery) ||
                    placeDescNorm.contains(normalizedQuery) ||
                    placeMuniNorm.contains(normalizedQuery)
            val matchesFavorites = !onlyFavorites || place.isFavorite

            matchesMunicipality && matchesCategory && matchesSearch && matchesFavorites
        }
    }

    private fun normalizeText(input: String): String {
        val normalized = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
