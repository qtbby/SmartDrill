package com.example.smartdrill.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    // A place on campus (gate, building, junction, evacuation area)
    public static class Node {
        public final int id;
        public final String name;
        public final double lat;
        public final double lng;
        public final boolean isEvacuation;

        // Store a map location and whether it is a valid evacuation destination.
        public Node(int id, String name, double lat, double lng, boolean isEvacuation) {
            this.id = id;
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.isEvacuation = isEvacuation;
        }
    }

    // A road between two places
    public static class Edge {
        public final int from;
        public final int to;
        public final double distance;        // meters
        public double hazardPenalty = 0;     // 0 = safe, bigger = more dangerous
        public boolean blocked = false;      // true = cannot be used at all

        // Create one directed copy of a road; Graph.connect creates its reverse copy too.
        Edge(int from, int to, double distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }

        // Combine physical distance and hazard penalty for route-cost comparisons.
        public double cost() {
            return distance + hazardPenalty;
        }
    }

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Map<Integer, List<Edge>> adjacency = new HashMap<>();

    // Register a place and initialize its outgoing-road list.
    public void addNode(Node node) {
        nodes.put(node.id, node);
        adjacency.put(node.id, new ArrayList<Edge>());
    }

    // Two-way road; the distance is computed from the coordinates
    public void connect(int a, int b) {
        Node na = nodes.get(a);
        Node nb = nodes.get(b);
        double d = distanceMeters(na.lat, na.lng, nb.lat, nb.lng);
        adjacency.get(a).add(new Edge(a, b, d));
        adjacency.get(b).add(new Edge(b, a, d));
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    // Return the registered campus places in insertion order.
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    // Return outgoing roads for a place so routing algorithms can traverse the graph.
    public List<Edge> getEdgesFrom(int id) {
        return adjacency.get(id);
    }

    // Mark the road between a and b as dangerous (both directions)
    public void setHazard(int a, int b, double penalty, boolean blocked) {
        markRoad(a, b, penalty, blocked);
        markRoad(b, a, penalty, blocked);
    }

    // Update the matching directed edge with the current hazard cost and closure state.
    private void markRoad(int from, int to, double penalty, boolean blocked) {
        for (Edge e : adjacency.get(from)) {
            if (e.to == to) {
                e.hazardPenalty = penalty;
                e.blocked = blocked;
            }
        }
    }

    // Reset every road to its normal state before applying a new hazard scenario.
    public void clearHazards() {
        for (List<Edge> edges : adjacency.values()) {
            for (Edge e : edges) {
                e.hazardPenalty = 0;
                e.blocked = false;
            }
        }
    }

    // Haversine formula: distance between two coordinates in meters
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}