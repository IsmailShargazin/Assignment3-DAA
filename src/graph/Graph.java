package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertices;
    private final boolean directed;
    private final List<Edge> edges;
    private final List<List<Edge>> adjacencyList;

    public Graph(int vertices, boolean directed) {
        if (vertices < 0) {
            throw new IllegalArgumentException(
                    "Number of vertices cannot be negative."
            );
        }

        this.vertices = vertices;
        this.directed = directed;
        this.edges = new ArrayList<>();
        this.adjacencyList = new ArrayList<>();

        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        validateVertex(from);
        validateVertex(to);

        Edge edge = new Edge(from, to, weight);

        edges.add(edge);
        adjacencyList.get(from).add(edge);

        if (!directed) {
            Edge reverseEdge = new Edge(to, from, weight);
            adjacencyList.get(to).add(reverseEdge);
        }
    }

    public int getVertices() {
        return vertices;
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> getNeighbors(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacencyList.get(vertex));
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) {
            throw new IllegalArgumentException(
                    "Invalid vertex: " + vertex
            );
        }
    }
}