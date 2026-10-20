package connectivity;

import graph.Edge;
import graph.Graph;
import util.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {

    public static class Result {
        public final List<Integer> order;
        public final boolean hasCycle;

        public Result(
                List<Integer> order,
                boolean hasCycle
        ) {
            this.order = List.copyOf(order);
            this.hasCycle = hasCycle;
        }
    }

    public static Result run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Topological sort requires a directed graph."
            );
        }

        int[] indegree = new int[
                graph.getVertices()
                ];

        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;
            indegree[edge.to]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();

        for (int vertex = 0;
             vertex < graph.getVertices();
             vertex++) {

            if (indegree[vertex] == 0) {
                queue.offer(vertex);
                metrics.queueOperations++;
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!queue.isEmpty()) {
            int vertex = queue.remove();

            metrics.queueOperations++;

            order.add(vertex);

            for (Edge edge : graph.getNeighbors(vertex)) {
                metrics.edgeExaminations++;

                indegree[edge.to]--;

                if (indegree[edge.to] == 0) {
                    queue.offer(edge.to);
                    metrics.queueOperations++;
                }
            }
        }

        boolean hasCycle =
                order.size() != graph.getVertices();

        return new Result(order, hasCycle);
    }
}