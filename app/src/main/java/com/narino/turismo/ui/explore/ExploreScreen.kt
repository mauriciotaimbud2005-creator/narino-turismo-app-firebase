package com.narino.turismo.ui.explore

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.narino.turismo.domain.model.Place
import com.narino.turismo.ui.theme.AccentLightGreen
import com.narino.turismo.ui.theme.CardBorderColor
import com.narino.turismo.ui.theme.CleanBackground
import com.narino.turismo.ui.theme.FavoriteRed
import com.narino.turismo.ui.theme.PrimaryGreen
import com.narino.turismo.ui.theme.RatingGold
import com.narino.turismo.ui.theme.SurfaceVariantClean
import com.narino.turismo.ui.theme.TextDark
import com.narino.turismo.ui.theme.TextSecondary

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBarContent(
                uiState = uiState,
                onMunicipalitySelected = { viewModel.onMunicipalitySelected(it) }
            )
        },
        bottomBar = {
            val currentTab = (uiState as? ExploreUiState.Success)?.currentTab ?: 0
            BottomNavigationBarContent(
                currentTab = currentTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        },
        containerColor = CleanBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ExploreUiState.Loading -> {
                    LoadingView()
                }

                is ExploreUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadPlaces() }
                    )
                }

                is ExploreUiState.Success -> {
                    ExploreSuccessContent(
                        state = state,
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onCategorySelected = { viewModel.onCategorySelected(it) },
                        onMunicipalitySelected = { viewModel.onMunicipalitySelected(it) },
                        onToggleFavorite = { placeId, currentFav ->
                            viewModel.onToggleFavorite(placeId, currentFav)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    uiState: ExploreUiState,
    onMunicipalitySelected: (String) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val successState = uiState as? ExploreUiState.Success
    val currentMunicipality = successState?.selectedMunicipality ?: "Todos"
    val availableMunicipalities = successState?.availableMunicipalities ?: listOf("Todos", "Pasto", "Ipiales", "Tumaco", "Túquerres")

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Descubre Nariño",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedMenu = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Municipio: $currentMunicipality",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar municipio",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    availableMunicipalities.forEach { muni ->
                        DropdownMenuItem(
                            text = { Text(text = muni) },
                            onClick = {
                                onMunicipalitySelected(muni)
                                expandedMenu = false
                            }
                        )
                    }
                }
            }
        },
        actions = {
            // User Avatar
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(38.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(1.5.dp, AccentLightGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar de Usuario",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryGreen
        )
    )
}

@Composable
fun ExploreSuccessContent(
    state: ExploreUiState.Success,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onMunicipalitySelected: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    var isMapView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar + Toggle Button (Lista / Mapa)
        SearchBarSection(
            query = state.searchQuery,
            onQueryChanged = onSearchQueryChanged,
            isMapView = isMapView,
            onToggleMapView = { isMapView = !isMapView }
        )

        // Categories horizontal filter chips
        CategoriesSection(
            selectedCategory = state.selectedCategory,
            categories = state.availableCategories,
            onCategorySelected = onCategorySelected
        )

        if (state.currentTab == 1) {
            // Municipalities filter row view
            MunicipalitiesHeaderSection(
                selectedMunicipality = state.selectedMunicipality,
                municipalities = state.availableMunicipalities,
                onMunicipalitySelected = onMunicipalitySelected
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vista de Mapa o Vista de Lista
        if (isMapView) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                MapViewComponent(
                    places = state.places,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            if (state.places.isEmpty()) {
                EmptyPlacesView(
                    isFavoriteTab = state.currentTab == 2
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = state.places,
                        key = { it.id }
                    ) { place ->
                        PlaceCard(
                            place = place,
                            onToggleFavorite = { onToggleFavorite(place.id, place.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarSection(
    query: String,
    onQueryChanged: (String) -> Unit,
    isMapView: Boolean,
    onToggleMapView: () -> Unit
) {
    var localText by remember(query) { mutableStateOf(query) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = localText,
            onValueChange = { newText ->
                localText = newText
                onQueryChanged(newText)
            },
            placeholder = {
                Text(
                    text = "¿Qué lugar quieres descubrir?",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = PrimaryGreen
                )
            },
            trailingIcon = {
                if (localText.isNotEmpty()) {
                    IconButton(onClick = {
                        localText = ""
                        onQueryChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar búsqueda",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = CardBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Botón Conmutador Lista / Mapa
        IconButton(
            onClick = onToggleMapView,
            modifier = Modifier
                .size(56.dp)
                .background(PrimaryGreen, shape = RoundedCornerShape(16.dp))
        ) {
            Icon(
                imageVector = if (isMapView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.Map,
                contentDescription = if (isMapView) "Ver como lista" else "Ver en mapa",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CategoriesSection(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = (category == "Todos" && selectedCategory == null) ||
                    (selectedCategory == category)

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (category == "Todos") {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category)
                    }
                },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceVariantClean,
                    labelColor = TextDark
                )
            )
        }
    }
}

@Composable
fun MunicipalitiesHeaderSection(
    selectedMunicipality: String,
    municipalities: List<String>,
    onMunicipalitySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Filtrar por Municipio",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(municipalities) { muni ->
                val isSelected = selectedMunicipality == muni
                FilterChip(
                    selected = isSelected,
                    onClick = { onMunicipalitySelected(muni) },
                    label = { Text(text = muni) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentLightGreen,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceVariantClean,
                        labelColor = TextDark
                    )
                )
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: Place,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header con la imagen proveniente del backend mBaaS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(SurfaceVariantClean)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(place.imageUrl.trim())
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { errorState ->
                        android.util.Log.e(
                            "CoilError",
                            "Fallo al cargar URL: '${place.imageUrl}' - Causa: ${errorState.result.throwable.message}"
                        )
                    }
                )

                // Oscurecimiento leve superior para dar contraste a los badges
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )

                // Etiqueta de Categoría (Arriba a la izquierda)
                Surface(
                    color = PrimaryGreen.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = place.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Botón de Favorito sincronizado con el mBaaS (Arriba a la derecha)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                ) {
                    Crossfade(
                        targetState = place.isFavorite,
                        label = "FavoriteTransition"
                    ) { isFav ->
                        if (isFav) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Quitar de favoritos",
                                tint = FavoriteRed
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Agregar a favoritos",
                                tint = TextDark
                            )
                        }
                    }
                }
            }

            // Detalles de la tarjeta
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Calificación
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = RatingGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Calificación",
                            tint = RatingGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.rating.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Ubicación + Botón "Cómo llegar" GPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = AccentLightGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.municipality,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    // Botón "Cómo llegar" directo a Google Maps
                    Button(
                        onClick = {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}&mode=d")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val fallbackIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}")
                                )
                                context.startActivity(fallbackIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cómo llegar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = place.description,
                    fontSize = 13.sp,
                    color = TextDark.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBarContent(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Explorar"
                )
            },
            label = { Text("Explorar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = AccentLightGreen.copy(alpha = 0.2f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "Municipios"
                )
            },
            label = { Text("Municipios") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = AccentLightGreen.copy(alpha = 0.2f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favoritos"
                )
            },
            label = { Text("Favoritos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = AccentLightGreen.copy(alpha = 0.2f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = PrimaryGreen,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando encantos de Nariño...",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "¡Ups! Ocurrió un inconveniente",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reintentar", color = Color.White)
            }
        }
    }
}

@Composable
fun EmptyPlacesView(
    isFavoriteTab: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isFavoriteTab) Icons.Default.FavoriteBorder else Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFavoriteTab) "Aún no tienes destinos favoritos guardados" else "No se encontraron destinos que coincidan con la búsqueda",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
