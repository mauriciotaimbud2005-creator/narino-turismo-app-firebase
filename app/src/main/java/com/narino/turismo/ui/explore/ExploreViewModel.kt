package com.narino.turismo.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narino.turismo.data.repository.PlacesRepositoryImpl
import com.narino.turismo.domain.repository.PlacesRepository
import com.narino.turismo.domain.usecase.GetPlacesUseCase
import com.narino.turismo.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: PlacesRepository = PlacesRepositoryImpl()
) : ViewModel() {

    private val getPlacesUseCase = GetPlacesUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var selectedMunicipality: String = "Todos"
    private var selectedCategory: String? = null
    private var searchQuery: String = ""
    private var currentTab: Int = 0

    init {
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                fetchFilteredPlaces()
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(
                    e.localizedMessage ?: "Ocurrió un error inesperado al cargar los destinos."
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        refreshData()
    }

    fun onMunicipalitySelected(municipality: String) {
        selectedMunicipality = municipality
        refreshData()
    }

    fun onCategorySelected(category: String?) {
        selectedCategory = if (selectedCategory == category) null else category
        refreshData()
    }

    fun onTabSelected(tabIndex: Int) {
        currentTab = tabIndex
        refreshData()
    }

    fun onToggleFavorite(placeId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(placeId, currentStatus)
                fetchFilteredPlaces()
            } catch (e: Exception) {
                // Sincronización transparente de UI
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            try {
                fetchFilteredPlaces()
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error("Error al filtrar la información")
            }
        }
    }

    private suspend fun fetchFilteredPlaces() {
        val onlyFavs = (currentTab == 2)
        val filterMuni = if (currentTab == 1 && selectedMunicipality == "Todos") {
            "Todos"
        } else {
            selectedMunicipality
        }

        val places = getPlacesUseCase(
            municipality = filterMuni,
            category = selectedCategory,
            searchQuery = searchQuery,
            onlyFavorites = onlyFavs
        )

        _uiState.value = ExploreUiState.Success(
            places = places,
            selectedMunicipality = selectedMunicipality,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            currentTab = currentTab,
            userEmail = repository.getCurrentUserEmail()
        )
    }
}
