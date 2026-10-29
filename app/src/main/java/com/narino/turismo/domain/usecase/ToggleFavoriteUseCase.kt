package com.narino.turismo.domain.usecase

import com.narino.turismo.domain.repository.PlacesRepository

class ToggleFavoriteUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(placeId: String, currentStatus: Boolean): Boolean {
        return repository.toggleFavorite(placeId, currentStatus)
    }
}
