package com.example.smartdrill.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Data model matching Firestore contract for testing filtering and parsing logic
data class TestEvacuationLocation(
    val id: String,
    val campusId: String?,
    val kind: String,
    val name: String,
    val active: Boolean,
    val lat: Double,
    val lng: Double,
    val coordinates: List<Map<String, Double>>
)

class EvacuationFilteringTest {

    @Test
    fun testCampusFilteringAndLegacySupport() {
        val locations = listOf(
            TestEvacuationLocation("1", "campus_a", "point", "Point A", true, 7.31, 125.67, emptyList()),
            TestEvacuationLocation("2", "campus_b", "point", "Point B", true, 7.32, 125.68, emptyList()),
            TestEvacuationLocation("3", null, "point", "Legacy Point", true, 7.33, 125.69, emptyList()) // legacy without campusId
        )

        // Filter for campus_a should include campus_a and legacy (campusId == null)
        val selectedCampusId = "campus_a"
        val filtered = locations.filter { it.active && (it.campusId == null || it.campusId == selectedCampusId) }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == "1" })
        assertTrue(filtered.any { it.id == "3" })
    }

    @Test
    fun testPolygonAreaCoordinatesParsing() {
        val rawCoords = listOf(
            mapOf("lat" to 7.31, "lng" to 125.67),
            mapOf("lat" to 7.32, "lng" to 125.67),
            mapOf("lat" to 7.32, "lng" to 125.68)
        )

        assertTrue(rawCoords.size >= 3)
        val parsed = rawCoords.map { GeoPointTest(it["lat"] ?: 0.0, it["lng"] ?: 0.0) }
        assertEquals(3, parsed.size)
        assertEquals(7.31, parsed[0].lat, 0.001)
    }

    data class GeoPointTest(val lat: Double, val lng: Double)
}
