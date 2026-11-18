package com.example.smartdrill.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RouteFinder {

    // Result bundles the selected route with its weighted cost and physical length.
    public static class Result {
        public final List<Graph.Node> path;
        public final double totalCost;       // distance + hazard penalties
        public final double totalDistance;   // meters

        Result(List<Graph.Node> path, double totalCost, double totalDistance) {
            this.path = path;
            this.totalCost = totalCost;
            this.totalDistance = totalDistance;
        }
    }

    private final Graph graph;

    // Reuse the supplied campus graph for all route searches performed by this finder.
    public RouteFinder(Graph graph) {
        this.graph = graph;
    }

    // Run Dijkstra over unblocked roads; return null when the destination is unreachable.
    public Result findSafestRoute(int startId, int endId) {
        Map<Integer, Double> best = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<double[]> queue =
                new PriorityQueue<double[]>((x, y) -> Double.compare(x[0], y[0]));

        best.put(startId, 0.0);
        queue.add(new double[]{0.0, startId});

        while (!queue.isEmpty()) {
            double[] current = queue.poll();
            double cost = current[0];
            int nodeId = (int) current[1];

            if (cost > best.getOrDefault(nodeId, Double.MAX_VALUE)) continue; // outdated entry
            if (nodeId == endId) break;

            for (Graph.Edge edge : graph.getEdgesFrom(nodeId)) {
                if (edge.blocked) continue;
                double newCost = cost + edge.cost();
                if (newCost < best.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    best.put(edge.to, newCost);
                    previous.put(edge.to, nodeId);
                    queue.add(new double[]{newCost, edge.to});
                }
            }
        }

        if (!best.containsKey(endId)) return null;

        // Walk backwards from the end to rebuild the path
        LinkedList<Graph.Node> path = new LinkedList<>();
        Integer step = endId;
        while (step != null) {
            path.addFirst(graph.getNode(step));
            step = previous.get(step);
        }

        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Graph.Node a = path.get(i);
            Graph.Node b = path.get(i + 1);
            distance += Graph.distanceMeters(a.lat, a.lng, b.lat, b.lng);
        }
        return new Result(path, best.get(endId), distance);
    }

    // Compare reachable evacuation destinations and return the one with the lowest route cost.
    public Result findNearestEvacuation(int startId) {
        Result bestResult = null;
        for (Graph.Node node : graph.getNodes()) {
            if (!node.isEvacuation) continue;
            Result r = findSafestRoute(startId, node.id);
            if (r != null && (bestResult == null || r.totalCost < bestResult.totalCost)) {
                bestResult = r;
            }
        }
        return bestResult;
    }
}