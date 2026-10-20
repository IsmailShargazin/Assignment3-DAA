package mst;

import graph.Edge;
import graph.Graph;
import util.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static MSTResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph."
            );
        }

        boolean[] visited = new boolean[
                graph.getVertices()
                ];

        List<Edge> selected = new ArrayList<>();

        PriorityQueue<Edge> queue = new PriorityQueue<>(
                (a, b) -> {
                    metrics.comparisons++;
                    return Integer.compare(a.weight, b.weight);
                }
        );

        long weight = 0;
        int components = 0;

        // Restart for each component if the graph is disconnected.
        for (int start = 0; start < graph.getVertices(); start++) {
            if (visited[start]) {
                continue;
            }

            components++;

            visit(
                    graph,
                    start,
                    visited,
                    queue,
                    metrics
            );

            while (!queue.isEmpty()) {
                Edge edge = queue.poll();

                metrics.queueOperations++;

                if (visited[edge.to]) {
                    continue;
                }

                selected.add(edge);
                weight += edge.weight;

                visit(
                        graph,
                        edge.to,
                        visited,
                        queue,
                        metrics
                );
            }
        }

        return new MSTResult(
                selected,
                weight,
                components
        );
    }

    private static void visit(
            Graph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<Edge> queue,
            Metrics metrics
    ) {
        visited[vertex] = true;

        for (Edge edge : graph.getNeighbors(vertex)) {
            metrics.edgeExaminations++;

            if (!visited[edge.to]) {
                queue.offer(edge);
                metrics.queueOperations++;
            }
        }
    }
}