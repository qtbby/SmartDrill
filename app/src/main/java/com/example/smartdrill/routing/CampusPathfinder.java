package com.example.smartdrill.routing;

/**
 * Part 5: CampusPathfinder
 * Combines DistanceCalculator, TimeEstimator, RouteCostFunction, and MaxFlowCapacityChecker
 * to compute the final safest route and guiding instructions for users.
 */
public class CampusPathfinder {

    private final RouteFinder routeFinder;

    public CampusPathfinder(Graph graph) {
        this.routeFinder = new RouteFinder(graph);
    }

    public RouteFinder.Result findBestEvacuationRoute(int startNodeId) {
        return routeFinder.findNearestEvacuation(startNodeId);
    }
}
