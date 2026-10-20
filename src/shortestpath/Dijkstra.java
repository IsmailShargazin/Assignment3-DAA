package shortestpath;

import graph.Edge;
import graph.Graph;
import util.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    public static class Result {
        public final long[] distances;

        private final int[] previous;

        public Result(
                long[] distances,
                int[] previous
        ) {
            this.distances = distances;
            this.previous = previous;
        }

        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= distances.length) {
                throw new IllegalArgumentException(
                        "Invalid target: " + target
                );
            }

            List<Integer> path = new ArrayList<>();

            if (distances[target] == INF) {
                return path;
            }

            for (int vertex = target;
                 vertex != -1;
                 vertex = previous[vertex]) {

                path.add(vertex);
            }

            Collections.reverse(path);

            return path;
        }
    }

    private static class State {
        final int vertex;
        final long distance;

        State(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    public static Result run(
            Graph graph,
            int source,
            Metrics metrics
    ) {
        if (source < 0 || source >= graph.getVertices()) {
            throw new IllegalArgumentException(
                    "Invalid source: " + source
            );
        }

        // Reject negative weights before running the algorithm.
        for (Edge edge : graph.getEdges()) {
            metrics.edgeExaminations++;

            if (edge.weight < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra requires non-negative weights."
                );
            }
        }

        long[] distances = new long[
                graph.getVertices()
                ];

        int[] previous = new int[
                graph.getVertices()
                ];

        Arrays.fill(distances, INF);
        Arrays.fill(previous, -1);

        distances[source] = 0;

        PriorityQueue<State> queue =
                new PriorityQueue<>((a, b) -> {
                    metrics.comparisons++;

                    return Long.compare(
                            a.distance,
                            b.distance
                    );
                });

        queue.offer(new State(source, 0));

        metrics.queueOperations++;

        while (!queue.isEmpty()) {
            State current = queue.poll();

            metrics.queueOperations++;

            // Skip an outdated queue entry.
            if (current.distance
                    != distances[current.vertex]) {
                continue;
            }

            for (Edge edge :
                    graph.getNeighbors(current.vertex)) {

                metrics.edgeExaminations++;

                long candidate =
                        current.distance + edge.weight;

                metrics.comparisons++;

                if (candidate < distances[edge.to]) {
                    distances[edge.to] = candidate;

                    previous[edge.to] = current.vertex;

                    queue.offer(
                            new State(edge.to, candidate)
                    );

                    metrics.queueOperations++;
                }
            }
        }

        return new Result(distances, previous);
    }
}