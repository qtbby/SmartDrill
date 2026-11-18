package com.example.smartdrill.routing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * MaxFlow answers one question:
 *   "What is the MAXIMUM number of people that can move from the buildings
 *    to the evacuation areas at the same time?"
 *
 * Think of the campus as water pipes:
 *   - Each walkway is a pipe, and its CAPACITY is how many people can walk
 *     through it per minute.
 *   - The buildings are where the water starts (the SOURCE).
 *   - The evacuation areas are where the water ends (the SINK).
 *
 * The method used is Edmonds-Karp. It repeats three steps until no path is left:
 *   1. Find a path from the source to the sink that still has room (BFS).
 *   2. Send as many people as the TIGHTEST walkway on that path allows.
 *   3. Update the walkways: less room forward, and more room to "undo" later.
 *
 * Example of how to use it:
 *   MaxFlow.Result r = MaxFlow.evacuationCapacity(
 *           CampusGraph.INSTANCE.getGraph(), java.util.Arrays.asList(23, 24, 25), 60);
 *   r.maxFlow     -> how many people per minute can reach safety
 *   r.bottlenecks -> the walkways that are full (widen these to help the most)
 */
public class MaxFlow {

    // A very large number that means "no limit".
    // Used for the fake start and end connections, so only the real walkways limit the flow.
    public static final long UNLIMITED = Long.MAX_VALUE;

    // -------------------------------------------------------------
    // PART 1: Small data holders
    // -------------------------------------------------------------

    // One walkway inside the algorithm.
    // It remembers its limit (capacity) and how many people it carries now (flow).
    private static class FlowEdge {
        final int from;       // where the walkway starts
        final int to;         // where the walkway ends
        final long capacity;  // the most people per minute it can carry
        long flow;            // how many people per minute it carries right now

        FlowEdge(int from, int to, long capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }

        // How much more this walkway can still carry
        long spare() {
            return capacity - flow;
        }
    }

    // One walkway in the final answer (this is what the app reads)
    public static class EdgeFlow {
        public final int from;
        public final int to;
        public final long flow;      // people per minute using it
        public final long capacity;  // people per minute it can hold

        EdgeFlow(int from, int to, long flow, long capacity) {
            this.from = from;
            this.to = to;
            this.flow = flow;
            this.capacity = capacity;
        }
    }

    // The final answer of the algorithm
    public static class Result {
        public final long maxFlow;               // total people per minute that can reach safety
        public final List<EdgeFlow> usedRoads;   // walkways that carry people
        public final List<EdgeFlow> bottlenecks; // walkways that are completely full and hold everyone back

        Result(long maxFlow, List<EdgeFlow> usedRoads, List<EdgeFlow> bottlenecks) {
            this.maxFlow = maxFlow;
            this.usedRoads = usedRoads;
            this.bottlenecks = bottlenecks;
        }
    }

    // -------------------------------------------------------------
    // PART 2: The network (the "pipes")
    // -------------------------------------------------------------

    private final int nodeCount;                              // how many places exist
    private final List<FlowEdge> edges = new ArrayList<>();   // every walkway, stored in pairs
    private final List<List<Integer>> adjacency = new ArrayList<>(); // for each place: which walkways leave it

    // Creates an empty network. Places are numbered 0 up to nodeCount - 1.
    public MaxFlow(int nodeCount) {
        if (nodeCount < 0) {
            throw new IllegalArgumentException("Node count cannot be negative");
        }
        this.nodeCount = nodeCount;
        for (int i = 0; i < nodeCount; i++) {
            adjacency.add(new ArrayList<Integer>());
        }
    }

    // Adds one one-way walkway from "from" to "to" that carries up to "capacity" people per minute.
    //
    // It also adds a hidden "reverse walkway" with capacity 0. Why?
    // The reverse walkway lets the algorithm UNDO an earlier choice
    // when it finds a better way to share the walkways.
    // Forward walkways are always at even positions (0, 2, 4...)
    // and their reverse is right after them (1, 3, 5...).
    public void addEdge(int from, int to, int capacity) {
        addEdge(from, to, (long) capacity);
    }

    public void addEdge(int from, int to, long capacity) {
        checkNode(from);
        checkNode(to);
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        adjacency.get(from).add(edges.size());
        edges.add(new FlowEdge(from, to, capacity));

        adjacency.get(to).add(edges.size());
        edges.add(new FlowEdge(to, from, 0));
    }

    // -------------------------------------------------------------
    // PART 3: The algorithm
    // -------------------------------------------------------------

    // Finds the maximum flow from "source" to "sink".
    public Result computeMaxFlow(int source, int sink) {
        checkNode(source);
        checkNode(sink);
        if (source == sink) {
            throw new IllegalArgumentException("Source and sink must be different");
        }

        // Start from zero: nobody is walking yet.
        for (FlowEdge e : edges) {
            e.flow = 0;
        }

        long totalFlow = 0;
        boolean[] reachable;   // places we can still reach from the source at the very end

        // Repeat until there is no path with spare room left.
        while (true) {

            // ---- STEP 1: Find a path that still has room (BFS) ----
            // BFS looks at the nearest places first, so the path found is the shortest one.
            // "parentEdge[x]" remembers which walkway we used to arrive at place x,
            // so we can walk backwards and rebuild the path later.
            int[] parentEdge = new int[nodeCount];
            Arrays.fill(parentEdge, -1);
            boolean[] visited = new boolean[nodeCount];
            Queue<Integer> queue = new ArrayDeque<>();

            visited[source] = true;
            queue.add(source);

            while (!queue.isEmpty() && !visited[sink]) {
                int place = queue.poll();
                for (int edgeIndex : adjacency.get(place)) {
                    FlowEdge e = edges.get(edgeIndex);
                    // Use this walkway only if it has spare room and leads somewhere new.
                    if (!visited[e.to] && e.spare() > 0) {
                        visited[e.to] = true;
                        parentEdge[e.to] = edgeIndex;
                        queue.add(e.to);
                    }
                }
            }

            // If the sink was never reached, every path is full, so we are finished.
            // "visited" now holds all places the source can still reach.
            // It is used below to find the bottlenecks.
            if (!visited[sink]) {
                reachable = visited;
                break;
            }

            // ---- STEP 2: Find the tightest walkway on this path ----
            // The whole group can only move as fast as the narrowest walkway.
            // Walk backwards from the sink to the source and keep the smallest spare room.
            long pushed = Long.MAX_VALUE;
            int place = sink;
            while (place != source) {
                FlowEdge e = edges.get(parentEdge[place]);
                pushed = Math.min(pushed, e.spare());
                place = e.from;
            }

            // ---- STEP 3: Send that many people along the path ----
            // Forward walkway: add the flow (less spare room).
            // Reverse walkway (index XOR 1): subtract the flow (more room to undo it later).
            place = sink;
            while (place != source) {
                int edgeIndex = parentEdge[place];
                edges.get(edgeIndex).flow += pushed;
                edges.get(edgeIndex ^ 1).flow -= pushed;
                place = edges.get(edgeIndex).from;
            }

            // Add to the total, then go back to step 1 and look for another path.
            totalFlow = Math.addExact(totalFlow, pushed);
        }

        // ---- Build the answer ----
        // Only the forward walkways (even positions) are real walkways.
        List<EdgeFlow> used = new ArrayList<>();
        List<EdgeFlow> bottlenecks = new ArrayList<>();
        for (int i = 0; i < edges.size(); i += 2) {
            FlowEdge e = edges.get(i);

            // A walkway that carries people is "used".
            if (e.flow > 0) {
                used.add(new EdgeFlow(e.from, e.to, e.flow, e.capacity));
            }

            // A bottleneck is a full walkway that connects the part of the campus
            // the source can still reach to the part it cannot.
            // Making these walkways wider is the only way to move more people.
            // (This is the "minimum cut", and it always matches the maximum flow.)
            if (reachable[e.from] && !reachable[e.to] && e.capacity > 0) {
                bottlenecks.add(new EdgeFlow(e.from, e.to, e.flow, e.capacity));
            }
        }

        return new Result(totalFlow, used, bottlenecks);
    }

    // -------------------------------------------------------------
    // PART 4: Connect it to your campus Graph
    // -------------------------------------------------------------

    // Calculates how many people per minute can reach the evacuation areas on your campus.
    //
    //   graph             - your campus map (places and walkways)
    //   sourceIds         - the places where people start (for example, the buildings)
    //   walkwayCapacity   - how many people per minute one walkway can carry (for example, 60)
    //
    // Blocked walkways (hazards) get no capacity, so a blocked road lowers the result.
    public static Result evacuationCapacity(Graph graph, List<Integer> sourceIds, int walkwayCapacity) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (sourceIds == null) {
            throw new IllegalArgumentException("Source IDs cannot be null");
        }
        if (walkwayCapacity < 0) {
            throw new IllegalArgumentException("Walkway capacity cannot be negative");
        }

        // Find the biggest place number so we can create two extra fake places after it.
        int maxId = -1;
        for (Graph.Node node : graph.getNodes()) {
            if (node.id < 0) {
                throw new IllegalArgumentException("Node IDs cannot be negative");
            }
            maxId = Math.max(maxId, node.id);
        }
        if (maxId > Integer.MAX_VALUE - 3) {
            throw new IllegalArgumentException("Node IDs are too large");
        }

        // Two fake places make the problem simple for the algorithm:
        //   superSource -> connects to every starting building
        //   superSink   -> every evacuation area connects to it
        // This turns "many starts, many ends" into "one start, one end".
        int superSource = maxId + 1;
        int superSink = maxId + 2;
        MaxFlow network = new MaxFlow(maxId + 3);
        long totalWalkwayCapacity = 0;

        for (Graph.Node node : graph.getNodes()) {

            // Copy each real walkway into the network, unless a hazard has blocked it.
            for (Graph.Edge road : graph.getEdgesFrom(node.id)) {
                if (!road.blocked) {
                    network.addEdge(road.from, road.to, walkwayCapacity);
                    totalWalkwayCapacity = Math.addExact(totalWalkwayCapacity, walkwayCapacity);
                }
            }
        }

        // The total capacity of real walkways is an upper bound on useful flow.
        // Using it avoids an arbitrary limit on the fake source/sink connections.
        for (Graph.Node node : graph.getNodes()) {
            if (node.isEvacuation) {
                network.addEdge(node.id, superSink, totalWalkwayCapacity);
            }
        }

        // The fake start place leads to every starting building with no limit.
        for (int id : sourceIds) {
            network.addEdge(superSource, id, totalWalkwayCapacity);
        }

        Result full = network.computeMaxFlow(superSource, superSink);

        // Remove the two fake places from the answer, so only real walkways remain.
        List<EdgeFlow> realUsed = new ArrayList<>();
        for (EdgeFlow e : full.usedRoads) {
            if (e.from != superSource && e.to != superSink) realUsed.add(e);
        }
        List<EdgeFlow> realBottlenecks = new ArrayList<>();
        for (EdgeFlow e : full.bottlenecks) {
            if (e.from != superSource && e.to != superSink) realBottlenecks.add(e);
        }

        return new Result(full.maxFlow, realUsed, realBottlenecks);
    }

    private void checkNode(int node) {
        if (node < 0 || node >= nodeCount) {
            throw new IllegalArgumentException("Node index is outside the network");
        }
    }
}