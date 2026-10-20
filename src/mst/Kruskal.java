package mst;

import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;
import util.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MSTResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph."
            );
        }

        List<Edge> sorted = new ArrayList<>(
                graph.getEdges()
        );

        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });

        DisjointSetUnion dsu = new DisjointSetUnion(
                graph.getVertices(),
                metrics
        );

        List<Edge> selected = new ArrayList<>();

        long weight = 0;

        for (Edge edge : sorted) {
            if (selected.size() == graph.getVertices() - 1) {
                break;
            }

            metrics.edgeExaminations++;

            if (dsu.union(edge.from, edge.to)) {
                selected.add(edge);
                weight += edge.weight;
            }
        }

        return new MSTResult(
                selected,
                weight,
                dsu.getComponents()
        );
    }
}