package com.narino.turismo.data.repository

import com.narino.turismo.data.remote.FirebaseDataSource
import com.narino.turismo.domain.model.Place
import com.narino.turismo.domain.repository.PlacesRepository

class PlacesRepositoryImpl(
    private val remoteDataSource: FirebaseDataSource = FirebaseDataSource()
) : PlacesRepository {

    private val localFavorites = mutableSetOf<String>()

    override suspend fun getPlaces(): List<Place> {
        val userId = remoteDataSource.getCurrentUserId()
        val remoteFavs = remoteDataSource.getUserFavoriteIds(userId)
        localFavorites.addAll(remoteFavs)

        val dtos = remoteDataSource.getPlaces()
        return dtos.map { dto ->
            val isFav = localFavorites.contains(dto.id) || dto.isFavorite
            dto.toDomain(isFavoriteOverride = isFav)
        }
    }

    override suspend fun toggleFavorite(placeId: String, currentStatus: Boolean): Boolean {
        val newStatus = !currentStatus
        if (newStatus) {
            localFavorites.add(placeId)
        } else {
            localFavorites.remove(placeId)
        }

        val userId = remoteDataSource.getCurrentUserId()
        remoteDataSource.toggleFavorite(userId, placeId, newStatus)
        return newStatus
    }

    override fun getCurrentUserEmail(): String {
        return "turista@narino.gov.co"
    }
}
