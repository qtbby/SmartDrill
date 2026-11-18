package com.example.smartdrill.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

// Label and coordinates for a map marker such as the user's location or a safe destination.
data class MapPoint(val name: String, val position: LatLon)

// Fallback map center used by generic map previews and call sites without a campus center.
val DefaultMapCenter = LatLon(7.3135228, 125.67033)

// Show campus routing guidance and keep the map's current GPS fix in Compose state.
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    userName: String = "User",
    hazardActive: Boolean = false,
    onNotificationsClick: () -> Unit = {},
    onTabSelected: (BottomTab) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Recalculate route guidance whenever the hazard state or current location changes.
    var userLocation by remember { mutableStateOf<LatLon?>(null) }
    val route = remember(hazardActive, userLocation) {
        userLocation?.let { CampusGraph.findRouteFrom(it.lat, it.lng, hazardActive) }
    }

    ScreenGradient(modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HomeHeader(userName, onNotificationsClick, onLogout)
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
                    onLocationChanged = { userLocation = it }
                )
            }
            AppBottomBar(BottomTab.MAP, onTabSelected)
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
    onLocationChanged: (LatLon?) -> Unit = {}
) {
    val start = userLocation?.let {
        MapPoint("Your current location", it)
    }
    val evacuations = remember {
        CampusGraph.graph.nodes
            .filter { it.isEvacuation }
            .map { MapPoint(it.name, LatLon(it.lat, it.lng)) }
    }
    val hazards = if (hazardActive) {
        listOf(MapPoint("Blocked road", CampusGraph.hazardCenter))
    } else emptyList()

    MapCard(
        modifier = modifier,
        center = CampusGraph.center,
        start = start,
        evacuations = evacuations,
        hazards = hazards,
        route = route?.path ?: emptyList(),
        interactive = interactive,
        onLocationChanged = onLocationChanged
    )
}

// Embed and manage the osmdroid map, location permission, route overlays, and map controls.
@Composable
fun MapCard(
    modifier: Modifier = Modifier,
    center: LatLon = DefaultMapCenter,
    start: MapPoint? = null,
    evacuations: List<MapPoint> = emptyList(),
    hazards: List<MapPoint> = emptyList(),
    route: List<LatLon> = emptyList(),
    interactive: Boolean = true,
    onLocationChanged: (LatLon?) -> Unit = {}
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

    // Update local permission state from the Android runtime permission result.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Construct the native map once, configure tile caching, and set its initial viewport.
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

    // Keep callbacks current while collecting the first GPS fix and maintaining map lifecycle.
    val currentOnLocationChanged by rememberUpdatedState(onLocationChanged)
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

    // Rebuild visible overlays from current inputs while retaining the native MapView instance.
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
                evacuations.forEach { map.overlays.add(pointMarker(map, it, 0xFF2E7D32.toInt(), 15)) }
                start?.let { map.overlays.add(pointMarker(map, it, 0xFFFF9800.toInt(), 15)) }

                map.invalidate()
            }
        )

        // When the map is not interactive (Home screen), let the page scroll instead
        if (!interactive) {
            Box(Modifier.fillMaxSize().pointerInput(Unit) {})
        } else {
            // Zoom In, Zoom Out, and Location Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Acquire / Center Location Button
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            locationOverlay.enableMyLocation()
                            val loc = locationOverlay.myLocation
                            if (loc != null) {
                                currentOnLocationChanged(LatLon(loc.latitude, loc.longitude))
                                mapView.controller.animateTo(loc)
                            } else {
                                locationOverlay.enableFollowLocation()
                            }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    containerColor = Color(0xFF0F3B4F),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Acquire Location")
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

// Create a density-correct circular marker with a tap label for a map point.
private fun pointMarker(map: MapView, point: MapPoint, color: Int, sizeDp: Int): Marker {
    val density = map.context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()

    val marker = Marker(map)
    marker.position = GeoPoint(point.position.lat, point.position.lng)
    marker.title = point.name
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.icon = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((2 * density).toInt(), 0xFFFFFFFF.toInt())
        setSize(sizePx, sizePx)
    }
    return marker
}

