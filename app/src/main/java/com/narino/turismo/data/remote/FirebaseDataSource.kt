package com.narino.turismo.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.narino.turismo.data.model.PlaceDto
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String {
        return auth?.currentUser?.uid ?: "user_default_123"
    }

    // Consulta directa a Cloud Firestore mBaaS con fallback a datos locales semilla
    suspend fun getPlaces(): List<PlaceDto> {
        val db = firestore ?: return getInitialSeedPlaces()
        return try {
            val snapshot = db.collection("lugares").get().await()
            if (snapshot.isEmpty) {
                getInitialSeedPlaces()
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(PlaceDto::class.java)
                    dto?.copy(id = if (dto.id.isBlank()) doc.id else dto.id)
                }
            }
        } catch (e: Exception) {
            getInitialSeedPlaces()
        }
    }

    // Consulta de subcolección de favoritos del usuario en mBaaS
    suspend fun getUserFavoriteIds(userId: String): List<String> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
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
        val db = firestore ?: return
        val favRef = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(placeId)

        try {
            if (isFavorite) {
                favRef.set(mapOf("addedAt" to System.currentTimeMillis())).await()
            } else {
                favRef.delete().await()
            }
        } catch (e: Exception) {
            // Manejo silencioso de excepciones
        }
    }

    private fun getInitialSeedPlaces(): List<PlaceDto> {
        return listOf(
            PlaceDto(
                id = "p1",
                name = "Santuario de Las Lajas",
                municipality = "Ipiales",
                category = "Cultura",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Santuario_de_Las_Lajas%2C_Ipiales%2C_Colombia%2C_2015-07-21%2C_DD_21-23_HDR.jpg/1200px-Santuario_de_Las_Lajas%2C_Ipiales%2C_Colombia%2C_2015-07-21%2C_DD_21-23_HDR.jpg",
                rating = 4.9,
                description = "Majestuoso santuario construido sobre el cañón del río Guáitara, considerado el milagro de Dios en el abismo.",
                latitude = 0.8177,
                longitude = -77.5858
            ),
            PlaceDto(
                id = "p2",
                name = "Laguna de La Cocha",
                municipality = "Pasto",
                category = "Naturaleza",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/La_Cocha_Lake_-_El_Encano.jpg/1200px-La_Cocha_Lake_-_El_Encano.jpg",
                rating = 4.8,
                description = "El segundo cuerpo de agua natural más grande de Colombia, rodeado de hermosas viviendas estilo suizo e Isla Corota.",
                latitude = 1.1394,
                longitude = -77.1517
            ),
            PlaceDto(
                id = "p3",
                name = "Playa El Morro",
                municipality = "Tumaco",
                category = "Recreación",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Tumaco_Beach.jpg/1200px-Tumaco_Beach.jpg",
                rating = 4.6,
                description = "Hermosa playa del Pacífico nariñense famosa por sus imponentes arcos naturales de piedra y deliciosa gastronomía marina.",
                latitude = 1.8159,
                longitude = -78.7567
            ),
            PlaceDto(
                id = "p4",
                name = "Volcán Azufral y Laguna Verde",
                municipality = "Túquerres",
                category = "Miradores",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Laguna_Verde_Azufral.jpg/1200px-Laguna_Verde_Azufral.jpg",
                rating = 4.9,
                description = "Impresionante cráter volcánico que alberga una de las lagunas verde esmeralda más espectaculares del mundo.",
                latitude = 1.0858,
                longitude = -77.6872
            ),
            PlaceDto(
                id = "p5",
                name = "Gastronomía Cuy Querido",
                municipality = "Pasto",
                category = "Gastronomía",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Cuy_asado_Pasto.jpg/1200px-Cuy_asado_Pasto.jpg",
                rating = 4.7,
                description = "Experiencia culinaria tradicional nariñense donde podrás degustar el plato insignia de la región, helados de paila y empanadas de añejo.",
                latitude = 1.2136,
                longitude = -77.2811
            ),
            PlaceDto(
                id = "p6",
                name = "Mirador Cruz de Fernando",
                municipality = "Túquerres",
                category = "Miradores",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Sabana_de_Tuquerres.jpg/1200px-Sabana_de_Tuquerres.jpg",
                rating = 4.5,
                description = "Vista panorámica inigualable sobre la sabana de Túquerres y las montañas andinas del sur de Colombia.",
                latitude = 1.0833,
                longitude = -77.6167
            )
        )
    }
}