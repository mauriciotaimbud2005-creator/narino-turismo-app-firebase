package com.narino.turismo.ui.explore

import com.narino.turismo.domain.model.Place

sealed interface ExploreUiState {
    data object Loading : ExploreUiState

    data class Success(
        val places: List<Place>,
        val selectedMunicipality: String = "Todos",
        val selectedCategory: String? = null,
        val searchQuery: String = "",
        val currentTab: Int = 0, // 0: Explorar, 1: Municipios, 2: Favoritos
        val userEmail: String = "turista@narino.gov.co",
        val availableMunicipalities: List<String> = listOf("Todos", "Pasto", "Ipiales", "Tumaco", "Túquerres"),
        val availableCategories: List<String> = listOf("Todos", "Naturaleza", "Miradores", "Recreación", "Gastronomía", "Cultura")
    ) : ExploreUiState

    data class Error(val message: String) : ExploreUiState
}
