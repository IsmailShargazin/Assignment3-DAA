import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import util.Metrics;

import java.util.Arrays;
import java.util.List;

public class AlgorithmTests {
    private static int passed = 0;

    public static void main(String[] args) {
        testDSU();
        testMST();
        testDisconnected();
        testMultipleMSTs();
        testTopologicalSort();
        testSCC();
        testDijkstra();
        testSingleVertex();
        testNegativeWeights();
        testGeneratedGraphs();

        System.out.println(
                "ALL TESTS PASSED (" + passed + " groups)"
        );
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void testDSU() {
        DisjointSetUnion dsu = new DisjointSetUnion(
                4, new Metrics()
        );

        check(dsu.union(0, 1), "First union failed");
        check(dsu.union(1, 2), "Second union failed");

        check(
                dsu.find(0) == dsu.find(2),
                "Vertices should be connected"
        );

        check(
                dsu.find(0) != dsu.find(3),
                "Vertex 3 should be separate"
        );

        check(
                !dsu.union(0, 2),
                "Repeated union should return false"
        );

        check(
                dsu.getComponents() == 2,
                "Wrong DSU component count"
        );

        pass("DSU");
    }

    private static Graph cityGraph() {
        Graph graph = new Graph(5, false);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 5);
        graph.addEdge(2, 3, 8);
        graph.addEdge(2, 4, 10);
        graph.addEdge(3, 4, 2);

        return graph;
    }

    private static void checkMST(
            Graph graph,
            long weight,
            int components
    ) {
        MSTResult kruskal = Kruskal.run(
                graph, new Metrics()
        );

        MSTResult prim = Prim.run(
                graph, new Metrics()
        );

        for (MSTResult result : List.of(kruskal, prim)) {
            check(
                    result.totalWeight == weight,
                    "Wrong MST/forest weight"
            );

            check(
                    result.components == components,
                    "Wrong component count"
            );

            check(
                    result.edges.size()
                            == graph.getVertices() - components,
                    "Wrong number of forest edges"
            );

            check(
                    result.isConnected() == (components == 1),
                    "Wrong connectivity result"
            );
        }
    }

    private static void testMST() {
        checkMST(cityGraph(), 10, 1);

        pass("Small graph MST");
    }

    private static void testDisconnected() {
        Graph graph = new Graph(5, false);

        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 3);

        checkMST(graph, 5, 3);

        Dijkstra.Result result = Dijkstra.run(
                graph, 0, new Metrics()
        );

        check(
                result.distances[1] == 2,
                "Wrong reachable distance"
        );

        for (int v = 2; v < 5; v++) {
            check(
                    result.distances[v] == Dijkstra.INF,
                    "Expected unreachable vertex"
            );

            check(
                    result.pathTo(v).isEmpty(),
                    "Unreachable path should be empty"
            );
        }

        pass("Disconnected graph and unreachable vertices");
    }

    private static void testMultipleMSTs() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);
        graph.addEdge(0, 2, 1);

        checkMST(graph, 3, 1);

        pass("Duplicate weights and multiple valid MSTs");
    }

    private static void checkOrder(Graph graph) {
        TopologicalSort.Result result = TopologicalSort.run(
                graph, new Metrics()
        );

        check(
                !result.hasCycle,
                "A DAG should not contain a cycle"
        );

        check(
                result.order.size() == graph.getVertices(),
                "Missing vertices"
        );

        int[] position = new int[graph.getVertices()];
        Arrays.fill(position, -1);

        for (int i = 0; i < result.order.size(); i++) {
            int v = result.order.get(i);

            check(
                    position[v] == -1,
                    "Repeated vertex in order"
            );

            position[v] = i;
        }

        for (Edge edge : graph.getEdges()) {
            check(
                    position[edge.from] < position[edge.to],
                    "Invalid topological order"
            );
        }
    }

    private static void testTopologicalSort() {
        Graph graph = new Graph(4, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        checkOrder(graph);

        graph.addEdge(3, 0, 1);

        check(
                TopologicalSort.run(
                        graph, new Metrics()
                ).hasCycle,
                "Cycle not detected"
        );

        pass("Topological order and cycle detection");
    }

    private static void testSCC() {
        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);

        List<List<Integer>> components = Kosaraju.run(
                graph, new Metrics()
        );

        check(
                components.size() == 3,
                "Expected three SCCs"
        );

        // owner[v] stores the component number of vertex v.
        int[] owner = new int[6];
        Arrays.fill(owner, -1);

        for (int i = 0; i < components.size(); i++) {
            for (int v : components.get(i)) {
                check(
                        owner[v] == -1,
                        "Vertex appears in more than one SCC"
                );

                owner[v] = i;
            }
        }

        for (int v = 0; v < 6; v++) {
            check(
                    owner[v] != -1,
                    "Vertex is missing from SCCs"
            );
        }

        check(
                owner[0] == owner[1] && owner[1] == owner[2],
                "Wrong SCC for 0, 1, 2"
        );

        check(
                owner[3] == owner[4],
                "Wrong SCC for 3, 4"
        );

        check(
                owner[0] != owner[3],
                "Separate SCCs were merged"
        );

        check(
                owner[5] != owner[0] && owner[5] != owner[3],
                "Vertex 5 should be separate"
        );

        pass("Strongly connected components");
    }

    private static void testDijkstra() {
        Dijkstra.Result result = Dijkstra.run(
                cityGraph(), 0, new Metrics()
        );

        long[] expected = {0, 3, 2, 8, 10};

        check(
                Arrays.equals(result.distances, expected),
                "Wrong shortest distances"
        );

        check(
                result.pathTo(4).equals(
                        List.of(0, 2, 1, 3, 4)
                ),
                "Wrong shortest path"
        );

        Graph directed = new Graph(3, true);

        directed.addEdge(0, 1, 0);
        directed.addEdge(1, 2, 2);
        directed.addEdge(0, 2, 5);

        result = Dijkstra.run(
                directed, 0, new Metrics()
        );

        check(
                result.distances[1] == 0
                        && result.distances[2] == 2,
                "Zero-weight route"
        );

        check(
                Dijkstra.run(
                        directed, 2, new Metrics()
                ).distances[0] == Dijkstra.INF,
                "Directed edges must not be reversed"
        );

        pass("Dijkstra distances, paths and zero weights");
    }

    private static void testSingleVertex() {
        checkMST(new Graph(1, false), 0, 1);

        Graph graph = new Graph(1, true);

        checkOrder(graph);

        check(
                Kosaraju.run(
                        graph, new Metrics()
                ).equals(List.of(List.of(0))),
                "Single SCC"
        );

        Dijkstra.Result result = Dijkstra.run(
                graph, 0, new Metrics()
        );

        check(
                result.distances[0] == 0,
                "Source distance should be zero"
        );

        check(
                result.pathTo(0).equals(List.of(0)),
                "Wrong source path"
        );

        graph.addEdge(0, 0, 0);

        check(
                TopologicalSort.run(
                        graph, new Metrics()
                ).hasCycle,
                "Self-loop not detected"
        );

        pass("Single vertex and self-loop");
    }

    private static void testNegativeWeights() {
        Graph graph = new Graph(2, false);

        graph.addEdge(0, 1, -3);

        checkMST(graph, -3, 1);

        boolean rejected = false;

        try {
            Dijkstra.run(graph, 0, new Metrics());
        } catch (IllegalArgumentException e) {
            rejected = true;
        }

        check(
                rejected,
                "Dijkstra should reject negative weights"
        );

        pass("Negative weights");
    }

    private static void testGeneratedGraphs() {
        int[][] sizes = {
                {10, 30},
                {100, 500},
                {1000, 5000}
        };

        for (int[] size : sizes) {
            int v = size[0];
            int e = size[1];

            Graph graph = GraphGenerator.connectedUndirected(
                    v, e, 42
            );

            Graph same = GraphGenerator.connectedUndirected(
                    v, e, 42
            );

            check(
                    graph.getVertices() == v
                            && graph.getEdges().size() == e,
                    "Wrong graph size"
            );

            check(
                    graph.getEdges().toString().equals(
                            same.getEdges().toString()
                    ),
                    "Seed mismatch"
            );

            MSTResult first = Kruskal.run(
                    graph, new Metrics()
            );

            MSTResult second = Prim.run(
                    graph, new Metrics()
            );

            check(
                    first.isConnected() && second.isConnected(),
                    "Generated graph disconnected"
            );

            check(
                    first.totalWeight == second.totalWeight,
                    "MST weights differ"
            );

            Graph dag = GraphGenerator.dag(v, e, 43);

            Graph cyclic = GraphGenerator.directedWithCycle(
                    v, e, 44
            );

            check(
                    dag.getEdges().size() == e
                            && cyclic.getEdges().size() == e,
                    "Directed sizes"
            );

            checkOrder(dag);

            check(
                    TopologicalSort.run(
                            cyclic, new Metrics()
                    ).hasCycle,
                    "Expected a cycle"
            );

            List<List<Integer>> components = Kosaraju.run(
                    cyclic, new Metrics()
            );

            boolean[] seen = new boolean[v];

            for (List<Integer> component : components) {
                for (int vertex : component) {
                    check(
                            !seen[vertex],
                            "Repeated SCC vertex"
                    );

                    seen[vertex] = true;
                }
            }

            for (boolean found : seen) {
                check(found, "Missing SCC vertex");
            }
        }

        pass("Generated graphs and reproducible inputs");
    }
}