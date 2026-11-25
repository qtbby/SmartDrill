package com.example.smartdrill.routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A simple, student-friendly implementation of the Ford-Fulkerson (Edmonds-Karp) Max-Flow Algorithm.
 * 
 * What it does:
 * Calculates the maximum number of people or traffic volume that can flow from
 * a starting location (Source) to an evacuation exit (Sink) through campus paths
 * without exceeding road capacities.
 */
public class MaxFlowAlgorithm {

    // Represents a directed edge in the flow network
    public static class Edge {
        public int from;
        public int to;
        public int capacity;
        public int flow;
        public Edge residual; // Back-edge for residual graph

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    private final int numNodes;
    private final List<List<Edge>> adj;

    public MaxFlowAlgorithm(int numNodes) {
        this.numNodes = numNodes;
        this.adj = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            adj.add(new ArrayList<>());
        }
    }

    // Add a directed road with a specific capacity between two nodes
    public void addEdge(int from, int to, int capacity) {
        Edge forward = new Edge(from, to, capacity);
        Edge backward = new Edge(to, from, 0); // Reverse residual edge starts with 0 flow capacity

        forward.residual = backward;
        backward.residual = forward;

        adj.get(from).add(forward);
        adj.get(to).add(backward);
    }

    // Find the maximum flow from source to sink using Breadth-First Search (Edmonds-Karp)
    public int computeMaxFlow(int source, int sink) {
        int maxFlow = 0;

        // Keep finding paths from source to sink that still have available capacity
        while (true) {
            // Store the edge used to reach each node so we can trace back the path
            Edge[] parentEdge = new Edge[numNodes];
            boolean[] visited = new boolean[numNodes];
            Queue<Integer> queue = new LinkedList<>();

            queue.add(source);
            visited[source] = true;

            boolean foundPath = false;

            // Standard BFS to find an augmenting path
            while (!queue.isEmpty()) {
                int u = queue.poll();

                if (u == sink) {
                    foundPath = true;
                    break;
                }

                for (Edge edge : adj.get(u)) {
                    int v = edge.to;
                    // If not visited and remaining capacity > 0
                    if (!visited[v] && (edge.capacity - edge.flow) > 0) {
                        visited[v] = true;
                        parentEdge[v] = edge;
                        queue.add(v);
                    }
                }
            }

            // If no path from source to sink is found, max flow is reached!
            if (!foundPath) {
                break;
            }

            // Find the minimum remaining capacity (bottleneck) along the path we just found
            int pushFlow = Integer.MAX_VALUE;
            int curr = sink;
            while (curr != source) {
                Edge edge = parentEdge[curr];
                int remainingCapacity = edge.capacity - edge.flow;
                if (remainingCapacity < pushFlow) {
                    pushFlow = remainingCapacity;
                }
                curr = edge.from;
            }

            // Update the flow along the path and their residual back-edges
            curr = sink;
            while (curr != source) {
                Edge edge = parentEdge[curr];
                edge.flow += pushFlow;
                edge.residual.flow -= pushFlow;
                curr = edge.from;
            }

            // Add this path's flow to our total max flow
            maxFlow += pushFlow;
        }

        return maxFlow;
    }
}
