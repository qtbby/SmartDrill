package com.example.smartdrill.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartdrill.routing.CampusGraph
import com.example.smartdrill.routing.LatLon
import com.example.smartdrill.routing.RouteInfo
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// Label and coordinates for a map marker such as the user's location or a safe destination.
data class MapPoint(val name: String, val position: LatLon)

// Firestore evacuation location model (points and polygon areas)
data class FirestoreEvacuationLocation(
    val id: String = "",
    val kind: String = "point", // "point" or "area"
    val name: String = "",
    val description: String = "",
    val active: Boolean = true,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val coordinates: List<Map<String, Double>> = emptyList()
)

// DNSC campus center updated to latitude 7.313721, longitude 125.670175.
val DefaultMapCenter = LatLon(7.313721, 125.670175)

// Show campus routing guidance and keep the map's current GPS fix in Compose state.
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    hazardActive: Boolean = false,
    currentTheme: AppTheme = AppTheme.DARK_SLATE,
    onThemeChanged: (AppTheme) -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {}
) {
    var userLocation by remember { mutableStateOf<LatLon?>(null) }
    val route = remember(hazardActive, userLocation) {
        userLocation?.let { CampusGraph.findRouteFrom(it.lat, it.lng, hazardActive) }
    }

    // Live listener for evacuation locations from Firestore (active == true)
    var firestoreEvacuations by remember { mutableStateOf<List<FirestoreEvacuationLocation>>(emptyList()) }
    var selectedEvacuation by remember { mutableStateOf<FirestoreEvacuationLocation?>(null) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("evacuationLocations")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val kind = doc.getString("kind") ?: "point"
                            val name = doc.getString("name") ?: "Evacuation Point"
                            val desc = doc.getString("description") ?: ""
                            val active = doc.getBoolean("active") ?: true

                            var lat = 0.0
                            var lng = 0.0
                            var coords = emptyList<Map<String, Double>>()

                            if (kind == "point") {
                                lat = doc.getDouble("lat") ?: 0.0
                                lng = doc.getDouble("lng") ?: 0.0
                            } else {
                                val rawCoords = doc.get("coordinates") as? List<*>
                                if (rawCoords != null) {
                                    coords = rawCoords.mapNotNull { item ->
                                        val map = item as? Map<*, *>
                                        if (map != null) {
                                            val latVal = (map["lat"] as? Number)?.toDouble() ?: 0.0
                                            val lngVal = (map["lng"] as? Number)?.toDouble() ?: 0.0
                                            mapOf("lat" to latVal, "lng" to lngVal)
                                        } else null
                                    }
                                }
                            }

                            FirestoreEvacuationLocation(
                                id = doc.id,
                                kind = kind,
                                name = name,
                                description = desc,
                                active = active,
                                lat = lat,
                                lng = lng,
                                coordinates = coords
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    firestoreEvacuations = list
                }
            }
        onDispose {
            listener.remove()
        }
    }

    ScreenGradient(modifier, currentTheme) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader(currentTheme, onThemeChanged)
                Spacer(Modifier.height(16.dp))
                if (route != null) {
                    InfoLine("Route from your current location")
                    InfoLine("You will arrive at: ${route.arrivalText}")
                    InfoLine("Direction: ${route.directionText}")
                    InfoLine("Safe place: ${route.destinationName}")
                } else if (userLocation == null) {
                    InfoLine("Enable location to find a campus evacuation route.")
                } else {
                    InfoLine("You appear to be off campus. Campus routes are shown only nearby.")
                }
                Spacer(Modifier.height(12.dp))
                CampusMapCard(
                    modifier = Modifier.weight(1f),
                    route = route,
                    hazardActive = hazardActive,
                    userLocation = userLocation,
                    firestoreEvacuations = firestoreEvacuations,
                    onLocationChanged = { userLocation = it },
                    onEvacuationSelected = { selectedEvacuation = it }
                )
            }
            AppBottomBar(BottomTab.MAP, onTabSelected)
        }
    }

    // Selected evacuation location info overlay card
    selectedEvacuation?.let { evac ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${evac.name} (${evac.kind.uppercase()})",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    TextButton(onClick = { selectedEvacuation = null }) {
                        Text("Close", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = evac.description.ifBlank { "Designated safe evacuation zone." },
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Display one line of route status or location guidance using the dashboard text style.
@Composable
private fun InfoLine(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
}

// Translate campus graph destinations, hazards, and the computed route into map overlays.
@Composable
fun CampusMapCard(
    modifier: Modifier = Modifier,
    route: RouteInfo?,
    hazardActive: Boolean,
    interactive: Boolean = true,
    userLocation: LatLon? = null,
    firestoreEvacuations: List<FirestoreEvacuationLocation> = emptyList(),
    onLocationChanged: (LatLon?) -> Unit = {},
    onEvacuationSelected: (FirestoreEvacuationLocation) -> Unit = {}
) {
    val start = userLocation?.let {
        MapPoint("Your current location", it)
    }
    val hazards = if (hazardActive) {
        listOf(MapPoint("Blocked road", CampusGraph.hazardCenter))
    } else emptyList()

    MapCard(
        modifier = modifier,
        center = DefaultMapCenter,
        start = start,
        hazards = hazards,
        route = route?.path ?: emptyList(),
        firestoreEvacuations = firestoreEvacuations,
        interactive = interactive,
        onLocationChanged = onLocationChanged,
        onEvacuationSelected = onEvacuationSelected
    )
}

// Embed and manage the osmdroid map, location permission, route overlays, and map controls.
@Composable
fun MapCard(
    modifier: Modifier = Modifier,
    center: LatLon = DefaultMapCenter,
    start: MapPoint? = null,
    hazards: List<MapPoint> = emptyList(),
    route: List<LatLon> = emptyList(),
    firestoreEvacuations: List<FirestoreEvacuationLocation> = emptyList(),
    interactive: Boolean = true,
    onLocationChanged: (LatLon?) -> Unit = {},
    onEvacuationSelected: (FirestoreEvacuationLocation) -> Unit = {}
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(14.dp)

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val mapView = remember {
        val config = Configuration.getInstance()
        config.userAgentValue = "SmartDrillCampusApp/1.0 (school project; contact: albit.al@dnsc.edu.ph)"
        config.osmdroidBasePath = File(context.cacheDir, "osmdroid")
        config.osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(17.0)
            controller.setCenter(GeoPoint(center.lat, center.lng))
        }
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }

    val currentOnLocationChanged by rememberUpdatedState(onLocationChanged)
    val currentOnEvacuationSelected by rememberUpdatedState(onEvacuationSelected)

    LaunchedEffect(hasLocationPermission, locationOverlay) {
        if (hasLocationPermission) {
            locationOverlay.enableMyLocation()
            locationOverlay.myLocation?.let { location ->
                currentOnLocationChanged(LatLon(location.latitude, location.longitude))
                mapView.controller.animateTo(location)
            }
            locationOverlay.runOnFirstFix {
                locationOverlay.myLocation?.let { location ->
                    currentOnLocationChanged(LatLon(location.latitude, location.longitude))
                    mapView.post { mapView.controller.animateTo(location) }
                }
            }
        } else {
            currentOnLocationChanged(null)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape)
            .clip(shape)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { map ->
                map.overlays.clear()
                map.overlays.add(CopyrightOverlay(map.context))

                if (hasLocationPermission) {
                    locationOverlay.enableMyLocation()
                    map.overlays.add(locationOverlay)
                }

                if (route.size >= 2) {
                    val line = Polyline(map)
                    line.setPoints(route.map { GeoPoint(it.lat, it.lng) })
                    line.outlinePaint.color = 0xFF1E88C7.toInt()
                    line.outlinePaint.strokeWidth = 16f
                    map.overlays.add(line)
                }

                hazards.forEach { map.overlays.add(pointMarker(map, it, 0xFFE53935.toInt(), 18)) }

                // Render Firestore evacuation locations (points as markers, areas as polygons)
                firestoreEvacuations.forEach { evac ->
                    if (evac.kind == "point") {
                        val marker = Marker(map)
                        marker.position = GeoPoint(evac.lat, evac.lng)
                        marker.title = evac.name
                        marker.snippet = evac.description
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.icon = createMarkerIcon(map, 0xFF2E7D32.toInt(), 15)
                        marker.setOnMarkerClickListener { _, _ ->
                            currentOnEvacuationSelected(evac)
                            true
                        }
                        map.overlays.add(marker)
                    } else if (evac.kind == "area" && evac.coordinates.size >= 3) {
                        val polygon = Polygon(map)
                        polygon.points = evac.coordinates.map { GeoPoint(it["lat"]!!, it["lng"]!!) }
                        polygon.title = evac.name
                        polygon.snippet = evac.description
                        polygon.fillPaint.color = 0x442E7D32.toInt()
                        polygon.outlinePaint.color = 0xFF2E7D32.toInt()
                        polygon.outlinePaint.strokeWidth = 4f
                        polygon.setOnClickListener { _, _, _ ->
                            currentOnEvacuationSelected(evac)
                            true
                        }
                        map.overlays.add(polygon)
                    }
                }

                start?.let { map.overlays.add(pointMarker(map, it, 0xFFFF9800.toInt(), 15)) }

                map.invalidate()
            }
        )

        if (!interactive) {
            Box(Modifier.fillMaxSize().pointerInput(Unit) {})
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Return to Campus Center Button (Lat 7.313721, Lng 125.670175)
                FloatingActionButton(
                    onClick = {
                        mapView.controller.animateTo(GeoPoint(DefaultMapCenter.lat, DefaultMapCenter.lng))
                    },
                    modifier = Modifier.size(44.dp),
                    containerColor = Color(0xFF0F3B4F),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Return to Campus")
                }

                // Zoom In Button
                FloatingActionButton(
                    onClick = { mapView.controller.zoomIn() },
                    modifier = Modifier.size(44.dp),
                    containerColor = Color(0xFF0F3B4F),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                // Zoom Out Button
                FloatingActionButton(
                    onClick = { mapView.controller.zoomOut() },
                    modifier = Modifier.size(44.dp),
                    containerColor = Color(0xFF0F3B4F),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }
        }
    }
}

// Create a density-correct circular marker icon.
private fun createMarkerIcon(map: MapView, color: Int, sizeDp: Int): GradientDrawable {
    val density = map.context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()

    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((2 * density).toInt(), 0xFFFFFFFF.toInt())
        setSize(sizePx, sizePx)
    }
}

// Create a density-correct circular marker with a tap label for a map point.
private fun pointMarker(map: MapView, point: MapPoint, color: Int, sizeDp: Int): Marker {
    val marker = Marker(map)
    marker.position = GeoPoint(point.position.lat, point.position.lng)
    marker.title = point.name
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.icon = createMarkerIcon(map, color, sizeDp)
    return marker
}
