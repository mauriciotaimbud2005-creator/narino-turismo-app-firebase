package com.narino.turismo.ui.explore

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.narino.turismo.domain.model.Place
import com.narino.turismo.ui.theme.AccentLightGreen
import com.narino.turismo.ui.theme.PrimaryGreen
import com.narino.turismo.ui.theme.RatingGold
import com.narino.turismo.ui.theme.TextDark
import com.narino.turismo.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker as OsmMarker
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MapViewComponent(
    places: List<Place>,
    modifier: Modifier = Modifier,
    onPlaceClick: ((Place) -> Unit)? = null
) {
    val validPlaces = places.filter { it.latitude != 0.0 && it.longitude != 0.0 }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Ubicación de referencia (Pasto, Nariño) y ubicación GPS real obtenida del dispositivo
    val pastoCenter = remember { GeoPoint(1.2136, -77.2811) }
    var realUserLocation by remember { mutableStateOf<GeoPoint?>(null) }

    var selectedPlace by remember { mutableStateOf(validPlaces.firstOrNull()) }
    var useOpenStreetMapEngine by remember { mutableStateOf(true) }
    var isSatelliteMode by remember { mutableStateOf(false) }

    // Función para obtener la ubicación GPS de alta precisión en tiempo real
    fun fetchHighAccuracyLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            realUserLocation = GeoPoint(location.latitude, location.longitude)
                        } else {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    realUserLocation = GeoPoint(lastLoc.latitude, lastLoc.longitude)
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            // Manejo silencioso de excepciones
        }
    }

    // Launcher de solicitud de permisos en tiempo real de Android
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchHighAccuracyLocation()
        }
    }

    // Solicitar permisos y obtener ubicación al inicializar el componente
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName

        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            fetchHighAccuracyLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Ubicación activa del usuario para el cálculo de distancia exacta
    val activeUserLocation = realUserLocation ?: pastoCenter

    // Estado del mapa de Google Maps
    val googleCameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(activeUserLocation.latitude, activeUserLocation.longitude), 9.5f
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (useOpenStreetMapEngine) {
            // MOTOR 1: OpenStreetMap (Renderizado de calles reales sin requerir API Key)
            OsmMapViewContainer(
                context = context,
                validPlaces = validPlaces,
                selectedPlace = selectedPlace,
                userLocation = activeUserLocation,
                hasRealGps = realUserLocation != null,
                onPlaceSelected = { place ->
                    selectedPlace = place
                    onPlaceClick?.invoke(place)
                }
            )
        } else {
            // MOTOR 2: Google Maps SDK
            GoogleMapContainer(
                validPlaces = validPlaces,
                cameraPositionState = googleCameraState,
                isSatelliteMode = isSatelliteMode,
                onPlaceSelected = { place ->
                    selectedPlace = place
                    onPlaceClick?.invoke(place)
                }
            )
        }

        // --- OVERLAY SUPERIOR COMPACTO CON DETALLE Y DISTANCIA PRECISA ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Insignia compacta de cantidad de destinos
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${validPlaces.size} en mapa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            // Recuadro compacto de detalle del lugar seleccionado
            selectedPlace?.let { place ->
                val distanceKm = calculateDistanceKm(
                    activeUserLocation.latitude, activeUserLocation.longitude,
                    place.latitude, place.longitude
                )

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = place.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(RatingGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = RatingGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = place.rating.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${place.municipality} • ${place.category}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AccentLightGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = null,
                                            tint = AccentLightGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "a ${String.format(Locale.getDefault(), "%.1f", distanceKm)} km",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentLightGreen
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { selectedPlace = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- BOTONES LATERALES DE CONTROL DE MAPA Y MI UBICACIÓN REAL ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mi Ubicación GPS Actual
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = {
                        val hasFineLocation = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        val hasCoarseLocation = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation || hasCoarseLocation) {
                            fetchHighAccuracyLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi Ubicación GPS Actual",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Cambiar motor de mapa (OpenStreetMap con calles reales vs Google Maps)
            Surface(
                shape = CircleShape,
                color = if (useOpenStreetMapEngine) PrimaryGreen else Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = { useOpenStreetMapEngine = !useOpenStreetMapEngine }) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Cambiar motor",
                        tint = if (useOpenStreetMapEngine) Color.White else PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OsmMapViewContainer(
    context: Context,
    validPlaces: List<Place>,
    selectedPlace: Place?,
    userLocation: GeoPoint,
    hasRealGps: Boolean,
    onPlaceSelected: (Place) -> Unit
) {
    val osmMap = remember {
        OsmMapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)
            controller.setZoom(10.5)
            controller.setCenter(userLocation)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            osmMap.onDetach()
        }
    }

    // Carga e instantánea actualización del mapa y marcadores
    LaunchedEffect(validPlaces, selectedPlace, userLocation) {
        osmMap.overlays.clear()

        // Marcador distintivo de ubicación del usuario
        val userMarker = OsmMarker(osmMap).apply {
            position = userLocation
            title = if (hasRealGps) "📍 Tu Ubicación GPS Actual" else "Ubicación de Referencia (Nariño)"
            snippet = if (hasRealGps) "Coordenadas exactas obtenidas del dispositivo" else "Acepta los permisos de GPS para precisión total"
            setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
        }
        osmMap.overlays.add(userMarker)

        if (validPlaces.isNotEmpty()) {
            val points = mutableListOf<GeoPoint>()
            points.add(userLocation)

            validPlaces.forEach { place ->
                val point = GeoPoint(place.latitude, place.longitude)
                points.add(point)

                val marker = OsmMarker(osmMap).apply {
                    position = point
                    title = place.name
                    snippet = "${place.municipality} - ${place.category}"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onPlaceSelected(place)
                        showInfoWindow()
                        true
                    }
                }
                osmMap.overlays.add(marker)
            }

            if (selectedPlace != null) {
                osmMap.controller.animateTo(
                    GeoPoint(selectedPlace.latitude, selectedPlace.longitude),
                    13.5,
                    800L
                )
            } else if (points.isNotEmpty()) {
                val box = BoundingBox.fromGeoPoints(points)
                osmMap.zoomToBoundingBox(box, true, 80)
            }
        }
        osmMap.invalidate()
    }

    AndroidView(
        factory = { osmMap },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun GoogleMapContainer(
    validPlaces: List<Place>,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    isSatelliteMode: Boolean,
    onPlaceSelected: (Place) -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = if (isSatelliteMode) MapType.SATELLITE else MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    ) {
        validPlaces.forEach { place ->
            val markerPosition = LatLng(place.latitude, place.longitude)
            com.google.maps.android.compose.Marker(
                state = rememberMarkerState(key = place.id, position = markerPosition),
                title = place.name,
                snippet = "${place.municipality} - ${place.category}",
                onClick = {
                    onPlaceSelected(place)
                    false
                }
            )
        }
    }
}

// Fórmula geodésica de Haversine para distancia exacta en kilómetros
private fun calculateDistanceKm(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
