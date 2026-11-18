package com.example.smartdrill.routing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MaxFlowTest {

    // Verify Edmonds-Karp against the classic textbook network whose maximum flow is 23.
    @Test
    public void classicExample() {
        MaxFlow net = new MaxFlow(6);
        net.addEdge(0, 1, 16);
        net.addEdge(0, 2, 13);
        net.addEdge(1, 2, 10);
        net.addEdge(2, 1, 4);
        net.addEdge(1, 3, 12);
        net.addEdge(3, 2, 9);
        net.addEdge(2, 4, 14);
        net.addEdge(4, 3, 7);
        net.addEdge(3, 5, 20);
        net.addEdge(4, 5, 4);

        assertEquals(23, net.computeMaxFlow(0, 5).maxFlow);
    }
}