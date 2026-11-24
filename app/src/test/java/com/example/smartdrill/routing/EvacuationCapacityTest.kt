package com.example.smartdrill.routing

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvacuationCapacityTest {

    @Test
    fun testCapacityCalculationReturnsResult() {
        val result = EvacuationCapacity.calculateCapacity(
            graph = CampusGraph.graph,
            sourceIds = listOf(CampusGraph.START_ID),
            walkwayCapacity = 60
        )

        assertNotNull(result)
        assertTrue(result.maxCapacity > 0)
    }

    @Test
    fun testNearestEvacuationRouteIsFound() {
        val route = EvacuationCapacity.findNearestEvacuationRoute(
            graph = CampusGraph.graph,
            startId = CampusGraph.START_ID
        )

        assertNotNull(route)
        assertTrue(route?.path?.isNotEmpty() == true)
    }

    @Test
    fun testRouteEndsAtEvacuation() {
        val route = EvacuationCapacity.findNearestEvacuationRoute(
            graph = CampusGraph.graph,
            startId = CampusGraph.START_ID
        )

        val endNode = route?.path?.last()
        val isEvacuation = endNode?.let { CampusGraph.graph.getNode(it.id)?.isEvacuation ?: false }
        assertTrue("Route should end at an evacuation area", isEvacuation == true)
    }
}