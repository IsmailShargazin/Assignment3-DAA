import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import util.Metrics;

public class Main {

    public static void main(String[] args) {
        Graph city = new Graph(5, false);

        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 2);
        city.addEdge(1, 2, 1);
        city.addEdge(1, 3, 5);
        city.addEdge(2, 3, 8);
        city.addEdge(2, 4, 10);
        city.addEdge(3, 4, 2);

        MSTResult kruskal = Kruskal.run(
                city,
                new Metrics()
        );

        MSTResult prim = Prim.run(
                city,
                new Metrics()
        );

        System.out.println(
                "=== INFRASTRUCTURE PLANNING ==="
        );

        System.out.println(
                "Connected: " + kruskal.isConnected()
        );

        System.out.println(
                "Kruskal weight: " + kruskal.totalWeight
        );

        System.out.println(
                "Prim weight: " + prim.totalWeight
        );

        System.out.println(
                "Kruskal edges: " + kruskal.edges
        );

        System.out.println(
                "Prim edges: " + prim.edges
        );

        System.out.println(
                "\n=== ROUTES FROM DISTRICT 0 ==="
        );

        Dijkstra.Result routes = Dijkstra.run(
                city,
                0,
                new Metrics()
        );

        for (int vertex = 0;
             vertex < city.getVertices();
             vertex++) {

            if (routes.distances[vertex] == Dijkstra.INF) {
                System.out.println(
                        vertex + ": unreachable"
                );
            } else {
                System.out.println(
                        vertex
                                + ": distance="
                                + routes.distances[vertex]
                                + ", path="
                                + routes.pathTo(vertex)
                );
            }
        }

        Graph services = new Graph(5, true);

        services.addEdge(0, 1, 1);
        services.addEdge(0, 2, 1);
        services.addEdge(1, 3, 1);
        services.addEdge(2, 3, 1);
        services.addEdge(3, 4, 1);

        System.out.println(
                "\n=== SERVICE DEPENDENCIES ==="
        );

        TopologicalSort.Result order =
                TopologicalSort.run(
                        services,
                        new Metrics()
                );

        System.out.println(
                "DAG order: " + order.order
        );

        services.addEdge(4, 1, 1);

        order = TopologicalSort.run(
                services,
                new Metrics()
        );

        System.out.println(
                "After adding 4 -> 1, cycle detected: "
                        + order.hasCycle
        );

        System.out.println(
                "SCCs: "
                        + Kosaraju.run(
                        services,
                        new Metrics()
                )
        );

        Graph disconnected = new Graph(3, false);

        disconnected.addEdge(0, 1, 7);

        MSTResult forest = Prim.run(
                disconnected,
                new Metrics()
        );

        System.out.println(
                "\n=== DISCONNECTED EXAMPLE ==="
        );

        System.out.println(
                "Connected: " + forest.isConnected()
        );

        System.out.println(
                "Forest components: " + forest.components
        );

        System.out.println(
                "Forest weight (not a full MST): "
                        + forest.totalWeight
        );

        Dijkstra.Result partial = Dijkstra.run(
                disconnected,
                0,
                new Metrics()
        );

        System.out.println(
                "District 2: "
                        + (
                        partial.distances[2] == Dijkstra.INF
                                ? "unreachable"
                                : partial.distances[2]
                )
        );
    }
}