package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {

    public static Graph connectedUndirected(
            int vertices,
            int edges,
            long seed
    ) {
        validate(vertices, edges, false);

        if (edges < vertices - 1) {
            throw new IllegalArgumentException(
                    "A connected graph needs at least V - 1 edges."
            );
        }

        Random random = new Random(seed);

        Graph graph = new Graph(vertices, false);

        Set<Long> used = new HashSet<>();

        // Build a spanning tree first.
        for (int vertex = 1;
             vertex < vertices;
             vertex++) {

            addUnique(
                    graph,
                    vertex,
                    random.nextInt(vertex),
                    random,
                    used
            );
        }

        // Add the remaining roads.
        while (graph.getEdges().size() < edges) {
            addUnique(
                    graph,
                    random.nextInt(vertices),
                    random.nextInt(vertices),
                    random,
                    used
            );
        }

        return graph;
    }

    public static Graph dag(
            int vertices,
            int edges,
            long seed
    ) {
        validate(vertices, edges, false);

        Random random = new Random(seed);

        Graph graph = new Graph(vertices, true);

        Set<Long> used = new HashSet<>();

        int[] order = new int[vertices];

        for (int i = 0; i < vertices; i++) {
            order[i] = i;
        }

        // Shuffle vertex order.
        for (int i = vertices - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            int temp = order[i];
            order[i] = order[j];
            order[j] = temp;
        }

        // Every edge points forward in this order.
        while (graph.getEdges().size() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);

            if (a < b) {
                addUnique(
                        graph,
                        order[a],
                        order[b],
                        random,
                        used
                );
            }
        }

        return graph;
    }

    public static Graph directedWithCycle(
            int vertices,
            int edges,
            long seed
    ) {
        validate(vertices, edges, true);

        if (vertices < 3 || edges < 3) {
            throw new IllegalArgumentException(
                    "This generator needs V >= 3 and E >= 3."
            );
        }

        Random random = new Random(seed);

        Graph graph = new Graph(vertices, true);

        Set<Long> used = new HashSet<>();

        // Guarantee a directed cycle.
        addUnique(graph, 0, 1, random, used);
        addUnique(graph, 1, 2, random, used);
        addUnique(graph, 2, 0, random, used);

        while (graph.getEdges().size() < edges) {
            addUnique(
                    graph,
                    random.nextInt(vertices),
                    random.nextInt(vertices),
                    random,
                    used
            );
        }

        return graph;
    }

    private static void addUnique(
            Graph graph,
            int from,
            int to,
            Random random,
            Set<Long> used
    ) {
        if (from == to) {
            return;
        }

        int a = from;
        int b = to;

        if (!graph.isDirected() && a > b) {
            a = to;
            b = from;
        }

        long key =
                ((long) a << 32) | (b & 0xffffffffL);

        if (used.add(key)) {
            graph.addEdge(
                    from,
                    to,
                    1 + random.nextInt(100)
            );
        }
    }

    private static void validate(
            int vertices,
            int edges,
            boolean directed
    ) {
        long maximum =
                (long) vertices * (vertices - 1);

        if (!directed) {
            maximum /= 2;
        }

        if (vertices < 1
                || edges < 0
                || edges > maximum) {

            throw new IllegalArgumentException(
                    "Invalid graph size."
            );
        }
    }
}