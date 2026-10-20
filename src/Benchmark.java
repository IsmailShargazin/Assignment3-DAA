import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;
import util.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Benchmark {
    private static final int REPEATS = 5;
    private static final int WARMUPS = 20;
    private static final long SEED = 20261020L;

    public static void main(String[] args) throws IOException {
        Path folder = Path.of("results", "tables");

        Files.createDirectories(folder);

        try (
                PrintWriter raw = new PrintWriter(
                        Files.newBufferedWriter(
                                folder.resolve("raw_runs.csv")
                        )
                );

                PrintWriter summary = new PrintWriter(
                        Files.newBufferedWriter(
                                folder.resolve("summary.csv")
                        )
                )
        ) {
            raw.println(
                    "algorithm,graph_type,V,E,run,time_ns,"
                            + "edge_examinations,comparisons,"
                            + "queue_operations,find_calls,"
                            + "parent_traversals,union_calls"
            );

            summary.println(
                    "algorithm,graph_type,V,E,average_ns,"
                            + "edge_examinations,comparisons,"
                            + "queue_operations,find_calls,"
                            + "parent_traversals,union_calls"
            );

            int[][] sizes = {
                    {10, 30},
                    {100, 500},
                    {1000, 5000}
            };

            for (int[] size : sizes) {
                int v = size[0];
                int e = size[1];

                Graph roads = GraphGenerator.connectedUndirected(
                        v, e, SEED + v
                );

                Graph dag = GraphGenerator.dag(
                        v, e, SEED + v + 1
                );

                Graph cyclic = GraphGenerator.directedWithCycle(
                        v, e, SEED + v + 2
                );

                measure(
                        "Kruskal", "undirected_connected",
                        roads, raw, summary
                );

                measure(
                        "Prim", "undirected_connected",
                        roads, raw, summary
                );

                measure(
                        "Kosaraju", "directed_cyclic",
                        cyclic, raw, summary
                );

                measure(
                        "Kahn_DAG", "directed_DAG",
                        dag, raw, summary
                );

                measure(
                        "Kahn_Cyclic", "directed_cyclic",
                        cyclic, raw, summary
                );

                measure(
                        "Dijkstra", "directed_cyclic",
                        cyclic, raw, summary
                );
            }
        }

        try (
                PrintWriter info = new PrintWriter(
                        Files.newBufferedWriter(
                                folder.resolve("environment.txt")
                        )
                )
        ) {
            info.println(
                    "Java: " + System.getProperty("java.version")
            );

            info.println(
                    "VM: " + System.getProperty("java.vm.name")
            );

            info.println(
                    "OS: " + System.getProperty("os.name")
                            + " " + System.getProperty("os.arch")
            );

            info.println(
                    "Available processors: "
                            + Runtime.getRuntime().availableProcessors()
            );

            info.println("Base seed: " + SEED);

            info.println(
                    "Warmups per case: " + WARMUPS
            );

            info.println(
                    "Measured repeats per case: " + REPEATS
            );
        }

        System.out.println(
                "Saved CSV files in results/tables"
        );

        System.out.println(
                "Benchmark completed."
        );
    }

    // Run the selected algorithm and return a small result.
    private static long runAlgorithm(
            String name,
            Graph graph,
            Metrics metrics
    ) {
        switch (name) {
            case "Kruskal":
                return Kruskal.run(graph, metrics).totalWeight;

            case "Prim":
                return Prim.run(graph, metrics).totalWeight;

            case "Kosaraju":
                return Kosaraju.run(graph, metrics).size();

            case "Kahn_DAG":
            case "Kahn_Cyclic":
                TopologicalSort.Result order = TopologicalSort.run(
                        graph, metrics
                );

                if (order.hasCycle) {
                    return -1;
                }

                return order.order.size();

            case "Dijkstra":
                Dijkstra.Result paths = Dijkstra.run(
                        graph, 0, metrics
                );

                long distance = paths.distances[
                        graph.getVertices() - 1
                        ];

                if (distance == Dijkstra.INF) {
                    return -1;
                }

                return distance;

            default:
                throw new IllegalArgumentException(
                        "Unknown algorithm: " + name
                );
        }
    }

    private static void measure(
            String name,
            String type,
            Graph graph,
            PrintWriter raw,
            PrintWriter summary
    ) {
        // Warm up the JVM before measuring.
        for (int i = 0; i < WARMUPS; i++) {
            runAlgorithm(name, graph, new Metrics());
        }

        long totalTime = 0;
        long result = 0;

        Metrics total = new Metrics();

        for (int run = 1; run <= REPEATS; run++) {
            Metrics metrics = new Metrics();

            long start = System.nanoTime();

            result = runAlgorithm(name, graph, metrics);

            long time = System.nanoTime() - start;

            totalTime += time;
            total.add(metrics);

            raw.printf(
                    Locale.ROOT,
                    "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                    name,
                    type,
                    graph.getVertices(),
                    graph.getEdges().size(),
                    run,
                    time,
                    metrics.edgeExaminations,
                    metrics.comparisons,
                    metrics.queueOperations,
                    metrics.findCalls,
                    metrics.parentTraversals,
                    metrics.unionCalls
            );
        }

        double average = totalTime / (double) REPEATS;

        summary.printf(
                Locale.ROOT,
                "%s,%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                name,
                type,
                graph.getVertices(),
                graph.getEdges().size(),
                average,
                total.edgeExaminations / (double) REPEATS,
                total.comparisons / (double) REPEATS,
                total.queueOperations / (double) REPEATS,
                total.findCalls / (double) REPEATS,
                total.parentTraversals / (double) REPEATS,
                total.unionCalls / (double) REPEATS
        );

        System.out.printf(
                Locale.ROOT,
                "%s: V=%d E=%d average=%.2f ns result=%d%n",
                name,
                graph.getVertices(),
                graph.getEdges().size(),
                average,
                result
        );
    }
}