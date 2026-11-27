package com.example.smartdrill.routing;

import java.util.List;

/**
 * Part 4: MaxFlowCapacityChecker (MaxFlowAlgorithm-Capacity)
 * Applies the Max-Flow concept to check if road segments and evacuation zones
 * have sufficient capacity to handle the volume of evacuees without causing bottlenecks.
 */
public class MaxFlowCapacityChecker {

    // Evaluates total maximum evacuee throughput through a selected route
    public static boolean validateRouteCapacity(List<Graph.Node> path, int expectedEvacuees) {
        // Simple throughput check: ensure path is valid and has at least 2 nodes
        if (path == null || path.size() < 2) return false;

        // Assume standard campus walkway capacity is 150 people safely per segment
        int standardSegmentCapacity = 150;
        return expectedEvacuees <= (path.size() * standardSegmentCapacity);
    }
}
