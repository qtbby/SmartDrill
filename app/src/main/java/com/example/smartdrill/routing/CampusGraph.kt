package com.example.smartdrill.routing

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class LatLon(val lat: Double, val lng: Double)

// UI-facing route summary, including map geometry and short arrival/direction guidance.
data class RouteInfo(
    val path: List<LatLon>,
    val destinationName: String,
    val distanceMeters: Double,
    val arrivalText: String,
    val directionText: String
)

object CampusGraph {

    // ------------------------------------------------------------------
    // 3 REFERENCE POINTS: a spot on the DNSC campus map picture (x, y)
    // and its real coordinates (lat, lng).
    // The coordinates below are ROUGH estimates so the map lands on campus.
    // Replace the lat/lng of all three with real ones (see Step 2).
    // ------------------------------------------------------------------
    private class Anchor(val x: Double, val y: Double, val lat: Double, val lng: Double)

    private val anchors = listOf(
        Anchor(775.0, 190.0, 7.314586, 125.671390),    // Academic Building (center of the building)
        Anchor(1010.0, 335.0, 7.313955, 125.672450),   // DNSC Sports Complex (center)
        Anchor(293.0, 633.0, 7.312881, 125.669396)     // GAD Health and Wellness Complex (center)
    )

    // Solve value = a*x + b*y + c from the three map-to-coordinate reference points.
    private fun solveAffine(v1: Double, v2: Double, v3: Double): Triple<Double, Double, Double> {
        val (p1, p2, p3) = anchors
        val det = p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)
        val a = (v1 * (p2.y - p3.y) + v2 * (p3.y - p1.y) + v3 * (p1.y - p2.y)) / det
        val b = (v1 * (p3.x - p2.x) + v2 * (p1.x - p3.x) + v3 * (p2.x - p1.x)) / det
        val c = v1 - a * p1.x - b * p1.y
        return Triple(a, b, c)
    }

    private val latCoef = solveAffine(anchors[0].lat, anchors[1].lat, anchors[2].lat)
    private val lngCoef = solveAffine(anchors[0].lng, anchors[1].lng, anchors[2].lng)

    // Convert a pixel position in the campus image into latitude and longitude.
    private fun toLatLon(x: Double, y: Double) = LatLon(
        latCoef.first * x + latCoef.second * y + latCoef.third,
        lngCoef.first * x + lngCoef.second * y + lngCoef.third
    )

    // ------------------------------------------------------------------

    const val START_ID = 1                 // 1 = Gate (Guard Post 1), 2 = Gate (Guardhouse)
    private const val HAZARD_A = 3         // demo hazard: the road in front of the Admin Building
    private const val HAZARD_B = 4
    private const val MAX_START_SNAP_DISTANCE_METERS = 120.0

    val center = LatLon(7.313721, 125.670175)

    // Places are positions on the map picture. Evacuation points are marked true.
    val graph = Graph().apply {
        fun place(id: Int, name: String, x: Double, y: Double, evac: Boolean = false) {
            val p = toLatLon(x, y)
            addNode(Graph.Node(id, name, p.lat, p.lng, evac))
        }

        // Gates
        place(1, "Gate (Guard Post 1)", 640.0, 640.0)
        place(2, "Gate (Guardhouse)", 815.0, 640.0)

        // Roads and junctions
        place(3, "Admin Building front (west)", 650.0, 548.0)
        place(4, "Admin Building front (east)", 800.0, 545.0)
        place(5, "East road (south)", 830.0, 525.0)
        place(6, "East road (ILEGG)", 818.0, 450.0)
        place(7, "East road (IC Building)", 815.0, 375.0)
        place(8, "East road (north)", 815.0, 250.0)
        place(9, "Main road (Gymnasium)", 610.0, 480.0)
        place(10, "Main road (Navigation Building)", 610.0, 370.0)
        place(11, "Main road (north)", 612.0, 245.0)
        place(12, "Cafeteria junction", 520.0, 520.0)
        place(13, "Student Center junction", 505.0, 405.0)
        place(14, "IAAS Building 2 junction", 365.0, 385.0)
        place(15, "Chapel junction", 200.0, 385.0)
        place(16, "Cottage loop (north)", 108.0, 245.0)
        place(17, "Cottage loop (middle)", 108.0, 420.0)
        place(18, "Cottage loop (south)", 115.0, 545.0)
        place(19, "College Hostel road", 420.0, 575.0)
        place(22, "Oval entrance", 925.0, 440.0)

        // Buildings (good starting points later)
        place(23, "IC Building", 766.0, 374.0)
        place(24, "Learning and Information Resource Center", 750.0, 455.0)
        place(25, "Academic Building", 775.0, 195.0)
        place(26, "Gymnasium", 650.0, 453.0)
        place(27, "Administration Building", 700.0, 583.0)

        // The DNSC Sports Complex is the designated evacuation area.
        place(20, "DNSC Sports Complex", 1010.0, 350.0, true)
        place(21, "Sports Complex east path", 1100.0, 400.0)

        // Additional assembly areas marked on the campus evacuation map.
        place(28, "Food Technology Department Assembly Area", 245.0, 290.0, true)
        place(29, "DNSC Hostel Assembly Area", 420.0, 575.0, true)
        place(30, "Central Assembly Area", 585.0, 535.0, true)
        place(31, "Science Building Assembly Area", 825.0, 475.0, true)

        // Roads between places
        connect(1, 3); connect(2, 5); connect(3, 4); connect(4, 5)
        connect(5, 6); connect(6, 7); connect(7, 8)
        connect(3, 9); connect(9, 10); connect(10, 11); connect(11, 8); connect(10, 7)
        connect(3, 12); connect(12, 13); connect(13, 10); connect(12, 19)
        connect(13, 14); connect(14, 15); connect(15, 17); connect(17, 16); connect(17, 18); connect(18, 19)
        connect(22, 6); connect(22, 20); connect(20, 21)
        connect(23, 7); connect(24, 6); connect(25, 8); connect(26, 9); connect(27, 3)
        connect(28, 15)
        connect(29, 19)
        connect(30, 3); connect(30, 12)
        connect(31, 5); connect(31, 6)
    }

    private val finder = RouteFinder(graph)

    // Demo hazard marker: halfway along the blocked road
    val hazardCenter: LatLon
        get() {
            val a = graph.getNode(HAZARD_A)
            val b = graph.getNode(HAZARD_B)
            return LatLon((a.lat + b.lat) / 2, (a.lng + b.lng) / 2)
        }

    // Find an evacuation route from the configured demo start, applying current hazard state.
    fun findRoute(hazardActive: Boolean): RouteInfo? {
        applyHazard(hazardActive)

        val result = finder.findNearestEvacuation(START_ID) ?: return null

        return routeInfo(
            result.path.map { LatLon(it.lat, it.lng) },
            result.totalDistance,
            result.path
        )
    }

    // Snap a valid nearby GPS coordinate to the road network and route to the safest destination.
    fun findRouteFrom(latitude: Double, longitude: Double, hazardActive: Boolean): RouteInfo? {
        if (!latitude.isFinite() || latitude !in -90.0..90.0 ||
            !longitude.isFinite() || longitude !in -180.0..180.0
        ) {
            return null
        }

        applyHazard(hazardActive)
        val nearestNode = graph.nodes
            .filter { !it.isEvacuation }
            .minByOrNull { Graph.distanceMeters(latitude, longitude, it.lat, it.lng) }
            ?: return null
        val distanceToNetwork = Graph.distanceMeters(
            latitude,
            longitude,
            nearestNode.lat,
            nearestNode.lng
        )
        if (distanceToNetwork > MAX_START_SNAP_DISTANCE_METERS) return null

        val result = finder.findNearestEvacuation(nearestNode.id) ?: return null
        val path = listOf(LatLon(latitude, longitude)) +
            result.path.map { LatLon(it.lat, it.lng) }
        return routeInfo(path, result.totalDistance + distanceToNetwork, result.path)
    }

    // Reset stale road flags before applying the current demo closure to the graph.
    private fun applyHazard(hazardActive: Boolean) {
        graph.clearHazards()
        if (hazardActive) graph.setHazard(HAZARD_A, HAZARD_B, 500.0, true)
    }

    // Format route distance, estimated walking time, destination, and first-turn guidance for UI.
    private fun routeInfo(path: List<LatLon>, distance: Double, routeNodes: List<Graph.Node>): RouteInfo {
        val seconds = (distance / 1.4).roundToInt()   // walking ~1.4 m/s
        return RouteInfo(
            path = path,
            destinationName = routeNodes.last().name,
            distanceMeters = distance,
            arrivalText = "%d min %02d sec".format(seconds / 60, seconds % 60),
            directionText = firstTurn(routeNodes)
        )
    }

    // Compare the first two segment bearings and translate their angle into a turn instruction.
    private fun firstTurn(path: List<Graph.Node>): String {
        if (path.size < 3) return "Go Straight"
        var delta = bearing(path[1], path[2]) - bearing(path[0], path[1])
        while (delta > 180) delta -= 360
        while (delta < -180) delta += 360
        return when {
            delta > 30 -> "Turn Right"
            delta < -30 -> "Turn Left"
            else -> "Go Straight"
        }
    }

    // Calculate the initial compass bearing from one graph node to the next.
    private fun bearing(a: Graph.Node, b: Graph.Node): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        return Math.toDegrees(atan2(y, x))
    }
}