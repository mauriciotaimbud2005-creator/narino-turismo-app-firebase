package com.narino.turismo.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narino.turismo.data.repository.PlacesRepositoryImpl
import com.narino.turismo.domain.model.Place
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

    private val _rawPlaces = MutableStateFlow<List<Place>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Todos")
    val selectedMunicipality = MutableStateFlow("Todos")
    val isMapView = MutableStateFlow(false)
    val currentTab = MutableStateFlow(0) // 0: Explorar, 1: Municipios, 2: Favoritos

    init {
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                val places = getPlacesUseCase()
                _rawPlaces.value = places
                updateUiStateSuccess(places)
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(
                    e.localizedMessage ?: "Ocurrió un error inesperado al cargar los destinos."
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        refreshSuccessState()
    }

    fun onMunicipalitySelected(municipality: String) {
        selectedMunicipality.value = municipality
        refreshSuccessState()
    }

    fun onCategorySelected(category: String?) {
        val newCategory = if (category == null || category == "Todos") {
            "Todos"
        } else if (selectedCategory.value == category) {
            "Todos"
        } else {
            category
        }
        selectedCategory.value = newCategory
        refreshSuccessState()
    }

    fun onTabSelected(tabIndex: Int) {
        currentTab.value = tabIndex
        refreshSuccessState()
    }

    fun onToggleFavorite(placeId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(placeId, currentStatus)
                val places = getPlacesUseCase()
                _rawPlaces.value = places
                refreshSuccessState()
            } catch (e: Exception) {
                // Sincronización silenciosa
            }
        }
    }

    fun onFavoriteClick(place: Place) {
        onToggleFavorite(place.id, place.isFavorite)
    }

    private fun refreshSuccessState() {
        viewModelScope.launch {
            val filterCat = if (selectedCategory.value == "Todos") null else selectedCategory.value
            val filterMuni = if (currentTab.value == 1 && selectedMunicipality.value == "Todos") "Todos" else selectedMunicipality.value

            val filtered = getPlacesUseCase(
                municipality = filterMuni,
                category = filterCat,
                searchQuery = searchQuery.value,
                onlyFavorites = (currentTab.value == 2)
            )
            updateUiStateSuccess(filtered)
        }
    }

    private fun updateUiStateSuccess(places: List<Place>) {
        _uiState.value = ExploreUiState.Success(
            places = places,
            selectedMunicipality = selectedMunicipality.value,
            selectedCategory = if (selectedCategory.value == "Todos") null else selectedCategory.value,
            searchQuery = searchQuery.value,
            currentTab = currentTab.value,
            userEmail = repository.getCurrentUserEmail()
        )
    }
}
