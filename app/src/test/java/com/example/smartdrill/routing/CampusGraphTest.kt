package com.example.smartdrill.routing

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampusGraphTest {
    // A route beginning at a campus node should reach the configured evacuation destination.
    @Test
    fun routeIsAvailableFromCampusNetwork() {
        val start = CampusGraph.graph.getNode(CampusGraph.START_ID)

        val route = CampusGraph.findRouteFrom(start.lat, start.lng, false)

        assertNotNull(route)
        assertTrue(CampusGraph.graph.nodes
            .filter { it.isEvacuation }
            .any { it.name == route?.destinationName })
    }

    @Test
    fun allMappedAssemblyAreasAreReachableEvacuationDestinations() {
        val evacuationNodes = CampusGraph.graph.nodes.filter { it.isEvacuation }

        assertEquals(5, evacuationNodes.size)
        assertEquals(
            setOf(
                "DNSC Sports Complex",
                "Food Technology Department Assembly Area",
                "DNSC Hostel Assembly Area",
                "Central Assembly Area",
                "Science Building Assembly Area"
            ),
            evacuationNodes.map { it.name }.toSet()
        )
        evacuationNodes.forEach { destination ->
            assertNotNull(
                RouteFinder(CampusGraph.graph)
                    .findSafestRoute(CampusGraph.START_ID, destination.id)
            )
        }
    }

    // Reject GPS positions beyond the campus network's configured snap radius.
    @Test
    fun routeIsUnavailableWhenFarFromCampusNetwork() {
        val route = CampusGraph.findRouteFrom(7.4478, 125.8078, false)

        assertNull(route)
    }
}
