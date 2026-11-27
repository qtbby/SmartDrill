package com.example.smartdrill.routing;

/**
 * Part 3: RouteCostFunction
 * Explains WHY the algorithm chooses a specific path.
 * Combines physical distance with hazard penalties (e.g. if a road is blocked or dangerous,
 * its cost increases drastically so the algorithm avoids it).
 */
public class RouteCostFunction {

    public static double computeEdgeCost(double distanceMeters, boolean blocked, int hazardSeverity) {
        if (blocked) {
            return Double.MAX_VALUE; // Completely impassable
        }
        // Base cost is physical distance in meters.
        // Add heavy penalties if there is a hazard nearby.
        double penalty = hazardSeverity * 50.0;
        return distanceMeters + penalty;
    }
}
