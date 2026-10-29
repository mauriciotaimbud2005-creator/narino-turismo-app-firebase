package com.narino.turismo.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.narino.turismo.data.model.PlaceDto
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "user_default_123"
    }

    // Consulta directa y exclusiva a la colección en la nube (Cloud Firestore)
    suspend fun getPlaces(): List<PlaceDto> {
        return try {
            val snapshot = firestore.collection("lugares").get().await()
            snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(PlaceDto::class.java)
                dto?.copy(id = if (dto.id.isBlank()) doc.id else dto.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Consulta de subcolección de favoritos del usuario en mBaaS
    suspend fun getUserFavoriteIds(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Guardado y eliminación directa en Cloud Firestore mBaaS
    suspend fun toggleFavorite(userId: String, placeId: String, isFavorite: Boolean) {
        val favRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(placeId)

        if (isFavorite) {
            favRef.set(mapOf("addedAt" to System.currentTimeMillis())).await()
        } else {
            favRef.delete().await()
        }
    }
}