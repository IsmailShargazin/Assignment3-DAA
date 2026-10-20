package connectivity;

import graph.Edge;
import graph.Graph;
import util.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kosaraju {

    public static List<List<Integer>> run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "SCC requires a directed graph."
            );
        }

        int n = graph.getVertices();
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();

        // First DFS: save each vertex after its neighbors.
        for (int v = 0; v < n; v++) {
            if (!visited[v]) {
                dfsOrder(graph, v, visited, order, metrics);
            }
        }

        // Reverse every edge.
        Graph reversed = new Graph(n, true);

        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;
            reversed.addEdge(edge.to, edge.from, edge.weight);
        }

        visited = new boolean[n];

        List<List<Integer>> components = new ArrayList<>();

        // Second DFS: process vertices in reverse finish order.
        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);

            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();

                dfsComponent(
                        reversed, v, visited, component, metrics
                );

                components.add(component);
            }
        }

        return components;
    }

    private static void dfsOrder(
            Graph graph,
            int v,
            boolean[] visited,
            List<Integer> order,
            Metrics metrics
    ) {
        visited[v] = true;

        for (Edge edge : graph.getNeighbors(v)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to]) {
                dfsOrder(
                        graph, edge.to, visited, order, metrics
                );
            }
        }

        order.add(v);
    }

    private static void dfsComponent(
            Graph graph,
            int v,
            boolean[] visited,
            List<Integer> component,
            Metrics metrics
    ) {
        visited[v] = true;
        component.add(v);

        for (Edge edge : graph.getNeighbors(v)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to]) {
                dfsComponent(
                        graph, edge.to, visited, component, metrics
                );
            }
        }
    }
}